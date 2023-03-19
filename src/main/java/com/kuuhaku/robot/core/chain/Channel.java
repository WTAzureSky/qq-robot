package com.kuuhaku.robot.core.chain;

/**
 * @author by kuuhaku
 * @Date 2021/4/23 23:29
 * @Description
 */
public interface Channel {

    /**
     * 执行命令
     *
     * @param context 上下文
     * @return 是否继续执行下一个channel
     */
    boolean execute(ChannelContext context);

    /**
     * 唯一id
     *
     * @return id
     */
    String id();

    /**
     * 执行顺序
     *
     * @return 按从小到大执行
     */
    int order();

    /**
     * 描述
     *
     * @return 指令描述
     */
    String description();
}
