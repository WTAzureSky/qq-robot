package com.kuuhaku.robot.timer;

import com.kuuhaku.robot.config.Robot;
import com.kuuhaku.robot.service.BilibiliService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @Author by kuuhaku
 * @Date 2021/2/18 6:49
 * @Description 定时器
 */
@Configuration
@Slf4j
public class TimeJob {
    @Autowired
    private Robot robot;
    @Autowired
    private BilibiliService bilibiliService;

    /**
     * 每天 23:00:00 执行
     */
    // @Scheduled(cron = "0 0 23 * * ?")
    public void sendNotify() {
        Bot bot = robot.getBot();
        ContactList<Group> groups = bot.getGroups();
        MessageChain messageChain = MessageUtils.newChain();
        messageChain = messageChain.plus("robot提醒你已经晚上11点了，早点睡");
        for (Group group : groups) {
            if (!group.getBotAsMember().isMuted()) {
                group.sendMessage(messageChain);
            }
        }
    }

    /**
     * 每天 07:00:00 执行
     */
    //@Scheduled(cron = "0 0 7 * * ?")
    public void doNotify() {
        Bot bot = robot.getBot();
        ContactList<Group> groups = bot.getGroups();
        MessageChain messageChain = MessageUtils.newChain();
        messageChain = messageChain.plus("robot提醒你已经早上7点了");
        for (Group group : groups) {
            log.info(Thread.currentThread().getName());
            if (!group.getBotAsMember().isMuted()) {
                group.sendMessage(messageChain);
            }
        }
    }

    @Scheduled(cron = "0 0/3 * * * ?")
    public void backgroundDumpBilibili() {
        bilibiliService.backDump();
    }
}
