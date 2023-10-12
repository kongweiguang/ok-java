package io.github.kongweiguang.ok.core;


import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static java.util.Objects.nonNull;

/**
 * 重试任务类
 *
 * @param <T> 任务结果类型
 * @author kongweiguang
 * @since 0.0.1
 */
public class Retry<T> {

    // region ----- retryFor

    /**
     * 重试根据指定的异常，没有返回值
     *
     * @param <T> 返回值类型
     * @param run 执行的方法 {@link Runnable}
     * @param ths 指定异常 {@link Throwable}，匹配任意一个异常时重试
     * @return 当前对象
     */
    @SafeVarargs
    public static <T> Retry<T> exceptions(final Runnable run, final Class<? extends Throwable>... ths) {
        return exceptions(() -> {
            run.run();
            return null;
        }, ths);
    }

    /**
     * 重试根据指定的异常，有返回值
     *
     * @param <T> 返回值类型
     * @param sup 执行的方法 {@link Supplier}
     * @param ths 指定异常 {@link Throwable}，匹配任意一个异常时重试
     * @return 当前对象
     */
    @SafeVarargs
    public static <T> Retry<T> exceptions(final Supplier<T> sup, final Class<? extends Throwable>... ths) {
        if (ths.length == 0) {
            throw new IllegalArgumentException("exs cannot be empty");
        }

        final BiPredicate<T, Throwable> strategy = (t, e) -> {
            if (nonNull(e)) {
                return Arrays.stream(ths).anyMatch(ex -> ex.isAssignableFrom(e.getClass()));
            }
            return false;
        };

        return new Retry<>(sup, strategy);
    }

    /**
     * 重试根据指定的策略，没有返回值
     *
     * @param <T>       返回值类型
     * @param run       执行的方法 {@link Runnable}
     * @param predicate 策略 {@link BiPredicate}，返回{@code true}时表示重试
     * @return 当前对象
     */
    public static <T> Retry<T> predicate(final Runnable run, final BiPredicate<T, Throwable> predicate) {
        return predicate(() -> {
            run.run();
            return null;
        }, predicate);
    }

    /**
     * 重试根据指定的策略，没有返回值
     *
     * @param <T>       返回值类型
     * @param sup       执行的方法 {@link  Supplier}
     * @param predicate 策略 {@link BiPredicate}，返回{@code true}时表示重试
     * @return 当前对象
     */
    public static <T> Retry<T> predicate(final Supplier<T> sup, final BiPredicate<T, Throwable> predicate) {
        return new Retry<>(sup, predicate);
    }
    // endregion

    /**
     * 执行结果
     */
    private T result;
    /**
     * 执行法方法
     */
    private final Supplier<T> sup;
    /**
     * 重试策略
     */
    private final BiPredicate<T, Throwable> predicate;
    /**
     * 重试次数，默认3次
     */
    private long maxAttempts = 3;
    /**
     * 重试间隔，默认1秒
     */
    private Duration delay = Duration.ofSeconds(1);

    /**
     * 构造方法，内部使用，调用请使用请用ofXXX
     *
     * @param sup       执行的方法
     * @param predicate 策略 {@link BiPredicate}，返回{@code true}时表示重试
     */
    private Retry(final Supplier<T> sup, final BiPredicate<T, Throwable> predicate) {
        Objects.requireNonNull(sup, "task parameter cannot be null");
        Objects.requireNonNull(predicate, "predicate parameter cannot be null");

        this.predicate = predicate;
        this.sup = sup;
    }

    /**
     * 最大重试次数
     *
     * @param maxAttempts 次数
     * @return 当前对象
     */
    public Retry<T> maxAttempts(final long maxAttempts) {
        if (this.maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }

        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * 重试间隔时间
     *
     * @param delay 间隔时间
     * @return 当前对象
     */
    public Retry<T> delay(final Duration delay) {
        Objects.requireNonNull(this.delay, "delay parameter cannot be null");

        this.delay = delay;
        return this;
    }

    /**
     * 获取结果
     *
     * @return 返回包装了结果的 {@link Optional}对象
     */
    public Optional<T> get() {
        return Optional.ofNullable(this.result);
    }


    /**
     * 同步执行重试方法
     *
     * @return 当前对象
     */
    public Retry<T> execute() {
        return doExecute();
    }

    /**
     * 开始重试
     *
     * @return 当前对象
     **/
    private Retry<T> doExecute() {
        Throwable th = null;

        while (--this.maxAttempts >= 0) {
            try {
                this.result = this.sup.get();
            } catch (final Throwable t) {
                th = t;
            }

            //判断重试
            if (!this.predicate.test(this.result, th)) {
                // 条件不满足时，跳出
                break;
            }

            try {
                sleep(delay.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }

}

