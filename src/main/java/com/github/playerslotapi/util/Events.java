package com.github.playerslotapi.util;

import com.github.playerslotapi.PlayerSlotAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 可取消式快速事件订阅工具
 * 不受所在类中的软依赖影响
 *
 * @param <T> 要订阅的事件
 * @author YiMiner
 */
public class Events<T extends Event> implements Listener, EventExecutor {

    private final AtomicLong expire = new AtomicLong(-1);
    private final Class<T> clazz;
    private final Function<T, Boolean> func;
    private boolean unregistered = false;

    public Events(Class<T> clazz, Function<T, Boolean> func) {
        this.func = func;
        this.clazz = clazz;
    }

    /**
     * 订阅一个事件. 事件处理函数无法自我取消事件的注册
     * 最高优先级、忽略取消
     *
     * @param clazz 事件类
     * @param func  事件处理函数. 没有返回值
     * @return 已经订阅的事件对象. 可以用unregister取消它
     */
    public static <T extends Event> Events<T> subscribe(Class<T> clazz, Consumer<T> func) {
        return subscribe(clazz, EventPriority.HIGHEST, true, func);
    }

    /**
     * 订阅一个事件. 事件处理函数可以自我取消事件的监听
     * 最高优先级、忽略取消
     *
     * @param clazz 事件类
     * @param func  事件处理函数. 返回false时取消事件注册
     * @return 已经订阅的事件对象. 可以用unregister取消它
     */
    public static <T extends Event> Events<T> subscribe(Class<T> clazz, Function<T, Boolean> func) {
        return subscribe(clazz, EventPriority.HIGHEST, true, func);
    }

    /**
     * 订阅一个事件. 事件处理函数不能自我取消事件的监听
     *
     * @param clazz           事件类
     * @param priority        事件优先级
     * @param ignoreCancelled 是否忽视已经取消的事件
     * @param func            事件处理函数. 没有返回值
     * @return 已经订阅的事件对象. 可以用unregister取消它
     */
    public static <T extends Event> Events<T> subscribe(Class<T> clazz, EventPriority priority, boolean ignoreCancelled, Consumer<T> func) {
        return subscribe(clazz, priority, ignoreCancelled, (event) -> {
            func.accept(event);
            return true;
        });
    }

    /**
     * 订阅一个事件. 事件处理函数可以自我取消事件的监听
     *
     * @param clazz           事件类
     * @param priority        事件优先级
     * @param ignoreCancelled 是否忽视已经取消的事件
     * @param func            事件处理函数. 返回false时取消事件监听
     * @return 已经订阅的事件对象. 可以用unregister取消它
     */
    public static <T extends Event> Events<T> subscribe(Class<T> clazz, EventPriority priority, boolean ignoreCancelled, Function<T, Boolean> func) {
        Events<T> factory = new Events<>(clazz, func);
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().registerEvent(clazz, factory, priority, factory, PlayerSlotAPI.getPlugin(), ignoreCancelled);
        } else {
            Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(), () -> Bukkit.getPluginManager().registerEvent(clazz, factory, priority, factory, PlayerSlotAPI.getPlugin(), ignoreCancelled));
        }
        return factory;
    }

    /**
     * 为事件监听器加上过期时间. 不调用这个方法时监听器永不过期
     *
     * @param expire 过期时间
     * @return 已经注册的事件对象
     */
    public Events<T> withTime(long expire) {
        this.expire.set(expire);
        return this;
    }

    /**
     * 判断当前事件监听器是否过期
     *
     * @return 是否过期
     */
    public boolean expired() {
        return expire.get() > 0 && System.currentTimeMillis() > expire.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Listener listener, Event event) {
        if (expired()) {
            unregister();
            return;
        }
        if (!clazz.isInstance(event)) {
            return;
        }
        if (!func.apply((T) event)) {
            unregister();
        }
    }

    /**
     * 取消事件监听
     */
    public void unregister() {
        if (unregistered) {
            return;
        }
        try {
            Method getHandlerListMethod = clazz.getMethod("getHandlerList");
            HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
            if (Bukkit.isPrimaryThread()) {
                handlerList.unregister(this);
            } else {
                Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(), () -> handlerList.unregister(this));
            }
        } catch (Exception ignored) {
        }
        unregistered = true;
    }


}
