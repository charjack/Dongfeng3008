package com.youchuang.dongfeng3008;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.youchuang.dongfeng3008.Utils.MediaUtils;
import com.youchuang.dongfeng3008.Utils.Mp4MediaUtils;
import com.youchuang.dongfeng3008.vo.Mp4Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements SurfaceHolder.Callback{

    SurfaceView surfaceView_video;
    private SurfaceHolder holder;
    private MediaPlayer mp;
    private boolean ishide = true;
    private TextView play_video_name;

    private String currentVideoPath = null;
    private int currentVideoProgress = 0;
    private String currentVideoName = null;
    private long currentVideoTotalTimenum =0;
    private String currentVideoTotalTime = null;
    public VideoUIUpdateListener videoUIUpdateListener;

    SeekBar seekBar2;
    LinearLayout progress_really_layout;
    TextView video_current_time,video_total_time;

    boolean ispause = false;
    Timer timer;
    MyVideoHandler myVideoHandler;

//    private int currentVideoIndexToPlay;

    private boolean selectfromuser = false;
    private boolean selectfromactivity = false;
    private int fullScreen = 0;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        videoUIUpdateListener = (VideoUIUpdateListener)activity;
    }

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        surfaceView_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BaseApp.ifFullScreenState == false){
                    if (ishide) {
                        //show
                        play_video_name.setText(currentVideoName.substring(0, currentVideoName.indexOf(".")));
                        play_video_name.setVisibility(View.VISIBLE);
                        progress_really_layout.setVisibility(View.VISIBLE);
                        ishide = false;
                    } else {
                        //hide
                        play_video_name.setVisibility(View.GONE);
                        progress_really_layout.setVisibility(View.GONE);
                        ishide = true;
                    }
                }else{
                    BaseApp.ifFullScreenState = false;
                    //显示小屏幕
                    videoUIUpdateListener.onVideoScreenChange(mp.getCurrentPosition());
                    //show
                    play_video_name.setText(currentVideoName.substring(0, currentVideoName.indexOf(".")));
                    play_video_name.setVisibility(View.VISIBLE);
                    progress_really_layout.setVisibility(View.VISIBLE);
                    ishide = false;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View video_view  =inflater.inflate(R.layout.fragment_video, container, false);
        surfaceView_video = (SurfaceView) video_view.findViewById(R.id.surfaceView_video);
        play_video_name = (TextView) video_view.findViewById(R.id.play_video_name);

        progress_really_layout = (LinearLayout) video_view.findViewById(R.id.progress_really_layout);
        seekBar2 = (SeekBar) video_view.findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    System.out.println("person change the progress...");
                    BaseApp.current_video_play_progress = progress;
                    seekBar.setProgress(progress);
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    mp.pause();
                    //改变暂停按钮
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.start();
                //改变暂停按钮
            }
        });
        video_current_time=(TextView) video_view.findViewById(R.id.video_current_time);
        video_total_time=(TextView) video_view.findViewById(R.id.video_total_time);
        seekBar2.setProgress(0);
        seekBar2.setMax((int) currentVideoTotalTimenum);
        video_total_time.setText(currentVideoTotalTime);
        myVideoHandler = new MyVideoHandler();
        holder = surfaceView_video.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        mp = new MediaPlayer();
        System.out.println("enter videoView creat...");
        return video_view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mp ==null){
            mp = new MediaPlayer();
        }
//        currentVideoIndexToPlay = BaseApp.current_video_play_num;
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setDisplay(holder);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //暂时不做处理，不自动进行下一个视频播放

//                if(mp.getCurrentPosition()+15 > MainActivity.mp4Infos.get(currentVideoIndexToPlay).getDuration()){
//                    MainActivity.mp4Infos.get(currentVideoIndexToPlay).setVideo_item_progressed(0);
//                }

                if (BaseApp.current_video_play_num + 1 >= MainActivity.mp4Infos.size()) {
                    mediaPlayer.stop();
                } else {
                    mediaPlayer.reset();
                    try {
                        BaseApp.current_video_play_num++;
                        mediaPlayer.setDataSource(MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getData());//设置播放视频源
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        currentVideoTotalTimenum = MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getDuration();
                        seekBar2.setMax((int) currentVideoTotalTimenum);
                        currentVideoTotalTime = Mp4MediaUtils.formatTime(currentVideoTotalTimenum);
                        video_total_time.setText(currentVideoTotalTime);
                        currentVideoName = MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getDisplay_name();
                        play_video_name.setText(currentVideoName.substring(0, currentVideoName.indexOf(".")));
//                        BaseApp.current_video_play_num  = currentVideoIndexToPlay;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Mp4Info mp4Info = MainActivity.mp4Infos.get(BaseApp.current_video_play_num);
        currentVideoPath = mp4Info.getData();
        currentVideoName  = mp4Info.getDisplay_name();
        currentVideoTotalTimenum = mp4Info.getDuration();   //
        currentVideoTotalTime = Mp4MediaUtils.formatTime(currentVideoTotalTimenum);
        System.out.println("video start-----" + currentVideoTotalTime);
        //第一次进入无法显示进度条和总时长
        seekBar2.setMax((int) currentVideoTotalTimenum);
        video_total_time.setText(currentVideoTotalTime);
        play_video_name.setText(currentVideoName.substring(0, currentVideoName.indexOf(".")));
        currentVideoProgress = (int)MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getVideo_item_progressed();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mp != null) {
                    myVideoHandler.sendEmptyMessage(1);
                }
            }
        }, 0, 500);

        if(selectfromactivity ==true) {
            selectfromactivity = false;
            play_video(currentVideoPath);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mp!=null)
        {
            System.out.println("enter video Onstop"+mp.getCurrentPosition());
            MainActivity.mp4Infos.get(BaseApp.current_video_play_num).setVideo_item_progressed(mp.getCurrentPosition());
            videoUIUpdateListener.onVideoProgressSave();
            if (mp.isPlaying()){
                mp.stop();
                mp.release();
                mp=null;
            }
        }
        timer.cancel();
        videoUIUpdateListener.onVideoStateChange();
    }

    public void playVideoFromMainactivity(int position,int progress){
        selectfromactivity = true;
        BaseApp.current_video_play_num = position;
        currentVideoProgress = progress;
        currentVideoProgress = (int)MainActivity.mp4Infos.get(position).getVideo_item_progressed();
    }

    public void playVideoFromUser(int position){
        selectfromuser = true;
        BaseApp.current_video_play_num = position;
        Mp4Info mp4Info = MainActivity.mp4Infos.get(BaseApp.current_video_play_num);
        currentVideoPath = mp4Info.getData();
        play_video(currentVideoPath);
    }

    public void playVideopre(){
        selectfromuser = true;
        if(BaseApp.current_video_play_num - 1 < 0){
            BaseApp.current_video_play_num = MainActivity.mp4Infos.size()-1;
        }else{
            BaseApp.current_video_play_num = BaseApp.current_video_play_num - 1;
        }
        Mp4Info mp4Info = MainActivity.mp4Infos.get(BaseApp.current_video_play_num);
        currentVideoPath = mp4Info.getData();
        play_video(currentVideoPath);
    }
    public void playVideonext(){
        selectfromuser = true;
        if(BaseApp.current_video_play_num + 1 >= MainActivity.mp4Infos.size()){
            BaseApp.current_video_play_num = 0;
        }else{
            BaseApp.current_video_play_num = BaseApp.current_video_play_num + 1;
        }
        Mp4Info mp4Info = MainActivity.mp4Infos.get(BaseApp.current_video_play_num);
        currentVideoPath = mp4Info.getData();
        play_video(currentVideoPath);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mp!=null){
            MainActivity.mp4Infos.get(BaseApp.current_video_play_num).setVideo_item_progressed(mp.getCurrentPosition());
            System.out.println("enter video Onstop"+MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getVideo_item_progressed());
            videoUIUpdateListener.onVideoProgressSave();
            if (mp.isPlaying()){
                mp.stop();
            }
            mp.release();
            mp=null;
        }
        timer.cancel();
    }

    public interface VideoUIUpdateListener{
        public void onVideoProgressSave();
        public void onVideoStateChange();
        public void onVideoScreenChange(int progress);
    }


    public void play_video(String Path) {

        //对播放视频状态进行更改
        currentVideoTotalTimenum = MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getDuration();
        seekBar2.setMax((int) currentVideoTotalTimenum);
        currentVideoTotalTime = Mp4MediaUtils.formatTime(currentVideoTotalTimenum);
        video_total_time.setText(currentVideoTotalTime);
        currentVideoName = MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getDisplay_name();
        play_video_name.setText(currentVideoName.substring(0, currentVideoName.indexOf(".")));
//        BaseApp.current_video_play_num  = currentVideoIndexToPlay;

        currentVideoProgress = (int)MainActivity.mp4Infos.get(BaseApp.current_video_play_num).getVideo_item_progressed();
        System.out.println("play_video:--------"+currentVideoProgress);

        if(selectfromuser == false){
            try {
                mp.reset();
                mp.setDataSource(Path);//设置播放视频源
                mp.prepare();
                mp.seekTo(currentVideoProgress);
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            selectfromuser = false;
            mp.stop();
            mp.reset();
            try {
                mp.setDataSource(Path);
                mp.prepare();
                mp.seekTo(currentVideoProgress);
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void start(){
        if(mp!=null && (!mp.isPlaying())){
            mp.start();//继续播放
            ispause = false;
        }
    }
    public void pause(){
        System.out.println("pause"+mp.getCurrentPosition());
        mp.pause();
        ispause = true;
    }

    public boolean isPlaying(){
        if(mp !=null){
            return mp.isPlaying();
        }
        return false;
    }

    class MyVideoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    if(mp!=null && isPlaying()){
                        currentVideoProgress = mp.getCurrentPosition();
                        MainActivity.mp4Infos.get(BaseApp.current_video_play_num).setVideo_item_progressed(currentVideoProgress);
                        seekBar2.setProgress(currentVideoProgress);
                        video_current_time.setText(Mp4MediaUtils.formatTime(currentVideoProgress));

                        if(BaseApp.ifopenliebiao == 0 && ishide && BaseApp.ifFullScreenState ==false){
                            fullScreen++;
                            if(fullScreen >= 10){
                                fullScreen = 0;
                                BaseApp.ifFullScreenState = true;
                                videoUIUpdateListener.onVideoScreenChange(mp.getCurrentPosition());
                            }
                        }else{
                            fullScreen = 0;
                        }
                    }
                    break;
            }
        }
    }
}