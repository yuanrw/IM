package com.yrw.im.common.function;

/**
 * Date: 2019-05-19
 * Time: 22:33
 *
 * @author yrw
 */
@FunctionalInterface
public interface ImBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept(T t, U u) throws Exception;
}
