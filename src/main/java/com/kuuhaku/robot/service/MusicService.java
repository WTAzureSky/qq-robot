package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.entity.music.NetEaseMusic;
import com.kuuhaku.robot.service.musicApi.NetEaseMusicApi;
import com.kuuhaku.robot.utils.MojiUtil;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MusicShare;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


/**
 * @Author by kuuhaku
 * @Date 2021/2/13 21:13
 * @Description 网易云音乐相关
 */
@Service
@Slf4j
public class MusicService {
    private final int maxTrackLength = 300;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private NetEaseMusicApi netEaseMusicApi;

    public List<NetEaseMusic> getMusicList(String musicName) {
        return netEaseMusicApi.getNetEaseMusicPage(musicName);
    }

    /**
     * 文字分享版本
     *
     * @param list
     * @param messageChain
     * @return
     */
    public MessageChain getMusicListMessage(List<NetEaseMusic> list, MessageChain messageChain) {
        messageChain = messageChain.plus("我找到了这些~!").plus("\n").plus("=== 网易云音乐 ===").plus("\n");
        for (int i = 0; i < list.size(); i++) {
            messageChain = messageChain.plus("" + (i + 1) + ". ").plus(list.get(i).getName() + " - ");
            List<String> artists = list.get(i).getArtists();
            if (!artists.isEmpty()) {
                messageChain = messageChain.plus(artists.get(0));
            }
            messageChain = messageChain.plus("\n");
        }
        return messageChain;
    }

    public String getMusicListImagePath(List<NetEaseMusic> list) {
        StringBuilder imageInfo = new StringBuilder("""
                我找到了这些~!
                === 网易云音乐 ===
                """);
        for (int i = 0; i < list.size(); i++) {
            imageInfo.append(i + 1).append(". ").append(list.get(i).getName()).append(" - ");
            List<String> artists = list.get(i).getArtists();
            imageInfo.append(String.join("/", artists));
            imageInfo.append("\n");
        }
        String imagePath = downloadService.getRandomPngPath();
        if (MojiUtil.createImage(imageInfo.toString(), imagePath)) {
            return imagePath;
        }
        return null;
    }


    public MusicShare getMusicCard(NetEaseMusic netEaseMusic) {
        return netEaseMusicApi.getMusicCard(netEaseMusic);
    }

    /**
     * 获取本地路径
     *
     * @param netEaseMusic
     * @return
     */
    public String getMusicPath(NetEaseMusic netEaseMusic) {
        String musicUrl = netEaseMusicApi.getMusicUrl(netEaseMusic);
        log.info("当前下载路径为:" + musicUrl);
        String mp3FileName = downloadService.getRandomPath() + ".mp3";
        String amrFileName = downloadService.getRandomPath() + ".amr";
        downloadService.download(musicUrl, mp3FileName);
        File source = new File(mp3FileName);
        File target = new File(amrFileName);
        // 为VIP歌曲下，文件为空
        if (source.length() <= 10L) {
            downloadService.deleteFile(mp3FileName);
            return null;
        }
        AudioAttributes audio = new AudioAttributes();
        int mp3TrackLength = getMp3TrackLength(source);
        log.info("当前歌曲[{}]的长度为[{}]s", netEaseMusic.getName(), mp3TrackLength);
        audio.setCodec("libamr_wb");
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        // 长于300S文件改成低质量格式，防止无法播放
        if (mp3TrackLength > maxTrackLength) {
            log.info("进行低音质转换");
            audio.setBitRate(15850);
        } else {
            audio.setBitRate(23850);
        }
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("amr");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
            downloadService.deleteFile(mp3FileName);
            downloadService.deleteFile(amrFileName);
            return null;
        }
        downloadService.deleteFile(mp3FileName);
        return amrFileName;
    }

    /**
     * 返回mp3文件播放时长，单位s
     *
     * @param mp3File
     * @return
     */
    private int getMp3TrackLength(File mp3File) {
        try {
            MP3File f = (MP3File) AudioFileIO.read(mp3File);
            MP3AudioHeader audioHeader = (MP3AudioHeader) f.getAudioHeader();
            return audioHeader.getTrackLength();
        } catch (Exception e) {
            log.info("读取mp3长度发生异常");
            return 1;
        }
    }

    public boolean mp3ToAmr(String srcPath, String tgtPath) {
        File source = new File(srcPath);
        File target = new File(tgtPath);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libamr_wb");
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        audio.setBitRate(23850);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("amr");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
