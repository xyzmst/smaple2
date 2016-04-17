package com.tencent.avsdk.control;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVAudioCtrl.AudioDataSourceType;
import com.tencent.av.sdk.AVAudioCtrl.AudioFrame;
import com.tencent.av.sdk.AVAudioCtrl.AudioFrameDesc;
import com.tencent.av.sdk.AVAudioCtrl.Delegate;
import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.Util;
import com.todoroo.aacenc.AACEncoder;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AVAudioControl {
    private Context mContext = null;


    public boolean bInputMixSend = false;
    public boolean bInputSpeakerSend = false;
    public boolean bOutMic = false;
    public boolean bOutSend = false;
    public boolean bOutPlay = false;
    public boolean bOutNetStream = false;
//	public String mixToSendFilename = "48000_2.pcm";
//	public String mixToPlayFilename = "48000_2.pcm";

    public String mixToSendFilename = "";
    public String mixToPlayFilename = "";

    private Object obj = new Object();

    private Delegate mDelegate = new Delegate() {
        @Override
        protected void onOutputModeChange(int outputMode) {
            super.onOutputModeChange(outputMode);
            mContext.sendBroadcast(new Intent(Util.ACTION_OUTPUT_MODE_CHANGE));
        }
    };

    AVAudioControl(Context context) {
        mContext = context;

    }

    void initAVAudio() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext)
                .getQavsdkControl();
        qavsdk.getAVContext().getAudioCtrl().setDelegate(mDelegate);

        setEnable(AudioDataSourceType.AUDIO_DATA_SOURCE_MIC, true);
        setEnable(AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM, true);
        registAudioDataCallback(AudioDataSourceType.AUDIO_DATA_SOURCE_MIC);
        registAudioDataCallback(AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM);

    }

    boolean getHandfreeChecked() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext)
                .getQavsdkControl();
        return qavsdk.getAVContext().getAudioCtrl().getAudioOutputMode() == AVAudioCtrl.OUTPUT_MODE_HEADSET;
    }

    String getQualityTips() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        AVAudioCtrl avAudioCtrl;
        if (qavsdk != null) {
            avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            return avAudioCtrl.getQualityTips();
        }

        return "";
    }


    public class audioFileInfo {
        public String filename;
        public int readIndex;
    }


    public int setEnable(int src_type, boolean enable) {
        audioDataEnable[src_type] = enable;
        Log.e("auido", "audio setEnable src_type = " + src_type + ", enable = " + enable);
        return 0;
    }


    public int registAudioDataCallback(int src_type) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.registAudioDataCallback(src_type, registAudioDataCompleteCallback);
        }
        return 1;
    }

    public int unregistAudioDataCallback(int src_type) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.unregistAudioDataCallback(src_type);
        }
        return 1;
    }

    public int unregistAudioDataCallbackAll() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.unregistAudioDataCallbackAll();
        }

        return 1;
    }

    public boolean setAudioDataFormat(int src_type, AudioFrameDesc audio_desc) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.setAudioDataFormat(src_type, audio_desc);
        }
        return false;
    }


    public AudioFrameDesc getAudioDataFormat(int src_type) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.getAudioDataFormat(src_type);
        }
        return null;
    }


    public int setAudioDataVolume(int src_type, float volume) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.setAudioDataVolume(src_type, volume);
        }
        return 1;
    }

    public float getAudioDataVolume(int src_type) {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
            AVAudioCtrl avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
            if (avAudioCtrl != null)
                return avAudioCtrl.getAudioDataVolume(src_type);
        }
        return 1;
    }


    public void removeOutputAudioFilename(int srctype) {
        audioDataFileName.remove(Integer.valueOf(srctype));
        audioDataEnable[srctype] = false;
        if (srctype == AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM) {
            audioNetStreamDataFileName.clear();
        }
    }

    public void resetAudio() {
        bInputMixSend = false;
        bInputSpeakerSend = false;
        bOutMic = false;
        bOutSend = false;
        bOutPlay = false;
        bOutNetStream = false;
        mixToSendFilename = "";
        mixToPlayFilename = "";
        unregistAudioDataCallbackAll();
        audioNetStreamDataFileName.clear();
    }

    private boolean[] audioDataEnable = new boolean[AudioDataSourceType.AUDIO_DATA_SOURCE_END];
    private int[] audioChannelDesc = new int[AudioDataSourceType.AUDIO_DATA_SOURCE_END];
    private int[] audioSamplerateDesc = new int[AudioDataSourceType.AUDIO_DATA_SOURCE_END];
    private HashMap<Integer, String> audioDataFileName = new HashMap<Integer, String>();

    private HashMap<String, String> audioNetStreamDataFileName = new HashMap<String, String>();
    private HashMap<Integer, Integer> audioDataFileReadIndex = new HashMap<Integer, Integer>();
    private AudioFrameDesc[] audioFrameDesc = new AudioFrameDesc[AudioDataSourceType.AUDIO_DATA_SOURCE_END];


    private AVAudioCtrl.RegistAudioDataCompleteCallback registAudioDataCompleteCallback = new AVAudioCtrl.RegistAudioDataCompleteCallback() {
        protected int onComplete(AudioFrame audioframe, int srcType) {

//            return origCallback(audioframe, srcType);
//            return newCallback(audioframe, srcType);
            if (!isInit) {
                String fullfilePath = Environment.getExternalStorageDirectory() + "/tencent/com/tencent/mobileqq/avsdk/" + System.currentTimeMillis() + ".aac";
                encoder.init(64000, audioframe.channelNum, audioframe.sampleRate, 16, fullfilePath);
                isInit = true;
                mWriteAccThread.start();
            }
            newDoDataCallback(audioframe, srcType);

            return AVError.AV_OK;
        }
    };

    ConcurrentLinkedQueue<byte[]> mic_queue = new ConcurrentLinkedQueue();
    ConcurrentLinkedQueue<byte[]> net_queue = new ConcurrentLinkedQueue();
    byte[] mic_byte;
    byte[] net_byte;

    int length = 48000 * 16 * 2;
    Thread mWriteAccThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                Log.e("=========", "start");
                if (mic_queue.size() > 0 && net_queue.size() > 0) {
                    if (mic_byte == null || net_byte == null) {
                        mic_byte = mic_queue.poll();
                        net_byte = net_queue.poll();
                    } else {
                        if (mic_byte.length < length && net_byte.length < length) {
                            mic_byte = byteMerger(mic_byte, mic_queue.poll());
                            net_byte = byteMerger(net_byte, net_queue.poll());
                        } else {
                            byte[] mix = mixAudio(mic_byte, net_byte);
                            encoder.encode(mix);
                            mic_byte = null;
                            net_byte = null;
                        }
                    }
                } else {
                    mic_queue.offer(new byte[3840]);
                    net_queue.offer(new byte[3840]);
                }

                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };


    /**
     * 原始的录制 callback函数
     *
     * @param audioframe
     * @param srcType
     * @return
     */
    private void newDoDataCallback(AudioFrame audioframe, int srcType) {
        if (audioframe == null)
            return;

        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM) {
            net_queue.offer(audioframe.data);
        } else if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_MIC) {
            mic_queue.offer(audioframe.data);
        }
    }


    /**
     * 原始的录制 callback函数
     *
     * @param audioframe
     * @param srcType
     * @return
     */
    private int newCallback(AudioFrame audioframe, int srcType) {
        if (audioframe == null)
            return AVError.AV_ERR_FAILED;

        String fullfilePath = "";
        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM) {

            if (TextUtils.isEmpty(audioframe.identifier))
                return AVError.AV_ERR_FAILED;
            fullfilePath = audioNetStreamDataFileName.get(audioframe.identifier);
            if (!audioDataEnable[srcType])
                return AVError.AV_ERR_FAILED;
            if (TextUtils.isEmpty(fullfilePath)) {
                fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                return writeAudioFile(fullfilePath, srcType, audioframe);
            } else {
                if ((audioSamplerateDesc[srcType] != audioframe.sampleRate) || (audioChannelDesc[srcType] != audioframe.channelNum)) {
                    fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                }
                return writeAudioFile(fullfilePath, srcType, audioframe);
            }
        } else {
            fullfilePath = audioDataFileName.get(Integer.valueOf(srcType));
            if (!audioDataEnable[srcType])
                return AVError.AV_ERR_FAILED;
            if (TextUtils.isEmpty(fullfilePath)) {
                fullfilePath = getOutputAudioFilePath(srcType, audioframe);

//                return writeAudioFile(fullfilePath, srcType, audioframe);
                return writeAacFile(fullfilePath, srcType, audioframe);
            } else {
                if ((audioSamplerateDesc[srcType] != audioframe.sampleRate) || (audioChannelDesc[srcType] != audioframe.channelNum)) {
                    fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                }
//                return writeAudioFile(fullfilePath, srcType, audioframe);
                return writeAacFile(fullfilePath, srcType, audioframe);
            }
        }
    }


    /**
     * 原始的录制 callback函数
     *
     * @param audioframe
     * @param srcType
     * @return
     */
    private int origCallback(AudioFrame audioframe, int srcType) {
        if (audioframe == null)
            return AVError.AV_ERR_FAILED;

        String fullfilePath = "";
        String filename = "";
        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_MIXTOSEND || srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_MIXTOPLAY) {
            synchronized (obj) {
                if (AudioDataSourceType.AUDIO_DATA_SOURCE_MIXTOSEND == srcType) {
                    filename = mixToSendFilename;
                } else if (AudioDataSourceType.AUDIO_DATA_SOURCE_MIXTOPLAY == srcType) {
                    filename = mixToPlayFilename;
                }

                if (TextUtils.isEmpty(filename))
                    return AVError.AV_ERR_FAILED;

                fullfilePath = Environment.getExternalStorageDirectory() + "/tencent/com/tencent/mobileqq/avsdk/" + filename;
                if (!audioDataEnable[srcType])
                    return AVError.AV_ERR_FAILED;
                return readAudioData(filename, fullfilePath, srcType, audioframe);
            }
        } else if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM) {

            if (TextUtils.isEmpty(audioframe.identifier))
                return AVError.AV_ERR_FAILED;
            fullfilePath = audioNetStreamDataFileName.get(audioframe.identifier);
            if (!audioDataEnable[srcType])
                return AVError.AV_ERR_FAILED;
            if (TextUtils.isEmpty(fullfilePath)) {
                fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                return writeAudioFile(fullfilePath, srcType, audioframe);
            } else {
                if ((audioSamplerateDesc[srcType] != audioframe.sampleRate) || (audioChannelDesc[srcType] != audioframe.channelNum)) {
                    fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                }
                return writeAudioFile(fullfilePath, srcType, audioframe);
            }
        } else {
            fullfilePath = audioDataFileName.get(Integer.valueOf(srcType));
            if (!audioDataEnable[srcType])
                return AVError.AV_ERR_FAILED;
            if (TextUtils.isEmpty(fullfilePath)) {
                fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                return writeAudioFile(fullfilePath, srcType, audioframe);
            } else {
                if ((audioSamplerateDesc[srcType] != audioframe.sampleRate) || (audioChannelDesc[srcType] != audioframe.channelNum)) {
                    fullfilePath = getOutputAudioFilePath(srcType, audioframe);
                }
                return writeAudioFile(fullfilePath, srcType, audioframe);
            }
        }
    }

    private int readAudioData(String filename, String fullfilePath, int srcType, AudioFrame audioframe) {
        int samplerate = 0;
        int channel = 0;
        int readIndex = 0;
        int readRet = AVError.AV_OK;
        RandomAccessFile raf = null;
        try {
            String strsamplerate = filename.substring(0, filename.indexOf('_'));
            String strchannel = filename.substring(filename.indexOf('_') + 1, filename.indexOf('_') + 2);
            samplerate = Integer.valueOf(strsamplerate);
            channel = Integer.valueOf(strchannel);
        } catch (Exception e) {
            // TODO: handle exception
            return AVError.AV_ERR_FAILED;
        }

        if (samplerate == 0 || channel == 0)
            return AVError.AV_ERR_FAILED;

        if (audioDataFileReadIndex.containsKey(Integer.valueOf(srcType))) {
            readIndex = audioDataFileReadIndex.get(Integer.valueOf(srcType));
        } else {
            readIndex = 0;
        }


        audioframe.sampleRate = samplerate;
        audioframe.channelNum = channel;
        audioframe.dataLen = samplerate * channel * 2 / 50;
        audioframe.bits = 16;
        if (audioFrameDesc[srcType] != null) {
            audioFrameDesc[srcType].bits = audioframe.bits;
            audioFrameDesc[srcType].sampleRate = audioframe.sampleRate;
            audioFrameDesc[srcType].channelNum = audioframe.channelNum;
            audioFrameDesc[srcType].srcTye = audioframe.srcTye;
        }

        try {
            raf = new RandomAccessFile(fullfilePath, "rw");
            int readBytes = 0;
            raf.seek(readIndex);
            readBytes = raf.read(audioframe.data, 0, audioframe.dataLen);
            if (readBytes < audioframe.dataLen) {
                raf.seek(0);
                readBytes = raf.read(audioframe.data, 0, audioframe.dataLen);
                audioDataFileReadIndex.put(Integer.valueOf(srcType), readBytes);
            } else {
                audioDataFileReadIndex.put(Integer.valueOf(srcType), (readIndex + readBytes));
            }
        } catch (Exception e) {
            // TODO: handle exception
            readRet = AVError.AV_ERR_FAILED;
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                // TODO: handle exception
                readRet = AVError.AV_ERR_FAILED;
            }
        }

        return readRet;
    }

    boolean isInit = false;

    AACEncoder encoder = new AACEncoder();

    private String getOutputAudioFilePath(int srcType, AudioFrame audioframe) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = sDateFormat.format(new java.util.Date());
        String fullfilePath = Environment.getExternalStorageDirectory() + "/tencent/com/tencent/mobileqq/avsdk/";
        File dir = new File(fullfilePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        switch (srcType) {
            case AudioDataSourceType.AUDIO_DATA_SOURCE_MIC:
                fullfilePath += "MIC_";
                break;
            case AudioDataSourceType.AUDIO_DATA_SOURCE_SEND:
                fullfilePath += "Send_";
                break;
            case AudioDataSourceType.AUDIO_DATA_SOURCE_PLAY:
                fullfilePath += "Play_";
                break;
            case AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM:
                fullfilePath += ("NetStream_" + audioframe.identifier + "_");
                break;
            default:
                break;
        }


//        fullfilePath += (audioframe.sampleRate + "_" + audioframe.channelNum + "_" + date + ".pcm");
        fullfilePath += (audioframe.sampleRate + "_" + audioframe.channelNum + "_" + date + ".aac");
        audioChannelDesc[srcType] = audioframe.channelNum;
        audioSamplerateDesc[srcType] = audioframe.sampleRate;


        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_MIC) {
            if (!isInit) {
                encoder.init(64000, audioframe.channelNum, audioframe.sampleRate, 16, fullfilePath);
                isInit = true;
            }
        }


        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_NETSTREM) {
            audioNetStreamDataFileName.put(audioframe.identifier, fullfilePath);
        } else {
            audioDataFileName.put(srcType, fullfilePath);
        }

        return fullfilePath;
    }


    private int writeAacFile(String filename, int srcType, AudioFrame audioframe) {
        int ret = AVError.AV_OK;
        if (srcType == AudioDataSourceType.AUDIO_DATA_SOURCE_MIC) {
//            encoder.encode(audioframe.data);
            if (mic_byte == null) {
                mic_byte = audioframe.data;
            } else {
                if (mic_byte.length < length) {
                    mic_byte = byteMerger(mic_byte, audioframe.data);
                } else {
                    encoder.encode(mic_byte);
                    mic_byte = null;
                }
            }
        }


        if (ret == AVError.AV_OK) {
            if (audioFrameDesc[srcType] != null) {
                audioFrameDesc[srcType].bits = audioframe.bits;
                audioFrameDesc[srcType].sampleRate = audioframe.sampleRate;
                audioFrameDesc[srcType].channelNum = audioframe.channelNum;
                audioFrameDesc[srcType].srcTye = audioframe.srcTye;
            }
        }
        return ret;
    }

    private int writeAudioFile(String filename, int srcType, AudioFrame audioframe) {
        int ret = AVError.AV_OK;
        RandomAccessFile randomFile = null;

        try {
            randomFile = new RandomAccessFile(filename, "rw");
            // 文件长度，字节数  
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。  
            randomFile.seek(fileLength);
            randomFile.write(audioframe.data, 0, audioframe.dataLen);
        } catch (Exception e) {
            // TODO: handle exception
            ret = AVError.AV_ERR_FAILED;
            e.printStackTrace();
        } finally {
            try {
                randomFile.close();
            } catch (Exception e2) {
                // TODO: handle exception
                ret = AVError.AV_ERR_FAILED;
            }
        }

        if (ret == AVError.AV_OK) {
            if (audioFrameDesc[srcType] != null) {
                audioFrameDesc[srcType].bits = audioframe.bits;
                audioFrameDesc[srcType].sampleRate = audioframe.sampleRate;
                audioFrameDesc[srcType].channelNum = audioframe.channelNum;
                audioFrameDesc[srcType].srcTye = audioframe.srcTye;
            }
        }
        return ret;
    }


    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    public void bytesToShorts(byte[] bytes, short[] pShorts) {
        if (bytes == null || bytes.length < 2) {
            return;
        }
        if (pShorts == null || pShorts.length < bytes.length / 2) {
            pShorts = new short[bytes.length / 2];
        }
        for (int i = 0; i < bytes.length / 2; i++) {
            pShorts[i] = getShort(bytes, i * 2);//swapBytes(outputByteArray[2*j + 1], outputByteArray[2*j ]);
        }
    }

    public short getShort(byte[] b, int index) {
        short l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
//        l |= ((long) b[index + 2] << 16);
//        l &= 0xffffff;
//        l |= ((long) b[index + 3] << 24);
        return l;
    }

    public short mixAudioByte(short[] samples, float weight) {
        int mixed = (int) (samples[0] * (1 - weight) + samples[1] * weight);
//        if(Math.abs(mixed)>Short.MAX_VALUE) {
//            Log.e("mixed1:", mixed + "");
//        }
        return (short) mixed;
    }

    public static void shortsToBytes(short[] shorts, byte[] bytes) {
        int i = 0;
        for (short s : shorts) {
            if (i * 2 + 1 <= bytes.length) {
                bytes[i * 2] = (byte) (s & 0xFF);
                bytes[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
                i++;
            } else {
                break;
            }
        }
    }

    public byte[] mixAudio(byte[] sample1, byte[] sample2) {
        short[] pShorts1 = new short[sample1.length / 2];
        short[] pShorts2 = new short[sample1.length / 2];
        byte[] result = new byte[sample1.length];
        bytesToShorts(sample1, pShorts1);
        bytesToShorts(sample2, pShorts2);
        short[] p = new short[pShorts1.length];
        for (int i = 0; i < pShorts1.length; i++) {
            int value = (pShorts1[i] + pShorts2[i]);
            p[i] = (short) value;
        }

        shortsToBytes(p, result);
        return result;

    }


}
