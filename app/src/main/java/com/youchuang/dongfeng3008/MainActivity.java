package com.youchuang.dongfeng3008;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.youchuang.dongfeng3008.Utils.MediaUtils;
import com.youchuang.dongfeng3008.Utils.Mp4MediaUtils;
import com.youchuang.dongfeng3008.Utils.MyBitMap;
import com.youchuang.dongfeng3008.Utils.MyImageView;
import com.youchuang.dongfeng3008.Utils.MyListView;
import com.youchuang.dongfeng3008.Utils.NativeImageLoader;
import com.youchuang.dongfeng3008.Utils.NoScrollGridView;
import com.youchuang.dongfeng3008.Utils.PicMediaUtils;
import com.youchuang.dongfeng3008.vo.Contents;
import com.youchuang.dongfeng3008.vo.Mp3Info;
import com.youchuang.dongfeng3008.vo.Mp4Info;
import com.youchuang.dongfeng3008.vo.PicInfo;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        MusicFragment.MusicUIUpdateListener,
        VideoFragment.VideoUIUpdateListener,
        PictureFragment.PicUIUpdateListener
{
    private static final String TAG = "MainActivity";

    RelativeLayout mRLayout;
    FrameLayout frame_content;
    ImageButton button_shangqu,button_bofang,button_xiaqu,button_play_mode,button_liebiao;
    ImageButton button_fangda,button_suoxiao;
    LinearLayout button_layout;
    Button button_music,button_video,button_pic;

    MusicFragment musicFragment;
    VideoFragment videoFragment;
    PictureFragment pictureFragment;
    List<Fragment> fragments = new ArrayList<>();
    RelativeLayout leibieliebiao;
    ListView musicvideolist;
    GridView gridview_id;
    LinearLayout loading_layout;
    ImageView frame_image;
    TextView no_music_resource;
    MymusiclistviewAdapter mymusiclistviewAdapter;
    MyvideolistviewAdapter myvideolistviewAdapter;

//    MyGridViewAdapter myGridViewAdapter;
    MyGridViewAdapter2 myGridViewAdapter2;

    static ArrayList<Mp3Info>  mp3Infos = new ArrayList<>();
    static ArrayList<Mp4Info> mp4Infos = new ArrayList<>();
    static ArrayList<PicInfo> picInfos = new ArrayList<>(); //进行异步加载
    private static MyHandler myHandler;
    private AnimationDrawable frameAnim;
    Mp3Info mp3Info_first;
    int list_size_first;
    int position_first;
    int musicplaymode_first;
    String last_music_id;

    public int[] music_play_mode_resource = {R.mipmap.suiji,R.mipmap.shunxu,R.mipmap.quanbuxunhuan,R.mipmap.danquxunhuan};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);

        mRLayout = (RelativeLayout) findViewById(R.id.content);
        frame_content = (FrameLayout) findViewById(R.id.frame_content);

        button_shangqu = (ImageButton) findViewById(R.id.button_shangqu);
        button_bofang = (ImageButton) findViewById(R.id.button_bofang);
        button_xiaqu = (ImageButton) findViewById(R.id.button_xiaqu);
        button_play_mode = (ImageButton) findViewById(R.id.button_play_mode);
        button_liebiao = (ImageButton) findViewById(R.id.button_liebiao);

        button_fangda = (ImageButton) findViewById(R.id.button_fangda);
        button_suoxiao = (ImageButton) findViewById(R.id.button_suoxiao);
        button_layout = (LinearLayout) findViewById(R.id.button_layout);

        button_music = (Button) findViewById(R.id.button_music);
        button_video = (Button) findViewById(R.id.button_video);
        button_pic = (Button) findViewById(R.id.button_pic);


        button_shangqu.setOnClickListener(this);
        button_bofang.setOnClickListener(this);
        button_xiaqu.setOnClickListener(this);
        button_play_mode.setOnClickListener(this);
        button_liebiao.setOnClickListener(this);
        button_music.setOnClickListener(this);
        button_video.setOnClickListener(this);
        button_pic.setOnClickListener(this);
        button_fangda.setOnClickListener(this);
        button_suoxiao.setOnClickListener(this);


        leibieliebiao = (RelativeLayout) findViewById(R.id.leibieliebiao);

        musicvideolist = (ListView) findViewById(R.id.musicvideolist);
        musicvideolist.setOnItemClickListener(this);
        loading_layout = (LinearLayout) findViewById(R.id.loading_layout);
        frame_image = (ImageView) findViewById(R.id.frame_image);
        no_music_resource = (TextView) findViewById(R.id.no_music_resource);
        gridview_id = (GridView) findViewById(R.id.gridview_id);

        gridview_id.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                button_layout.setBackgroundResource(R.mipmap.dilan_pic);
                button_fangda.setVisibility(View.VISIBLE);
                button_suoxiao.setVisibility(View.VISIBLE);
                button_play_mode.setVisibility(View.GONE);
                button_shangqu.setImageResource(R.mipmap.shangqu_pic);
                button_bofang.setImageResource(R.mipmap.zanting_pic);
                button_xiaqu.setImageResource(R.mipmap.xiaqu_pic);
                button_liebiao.setImageResource(R.mipmap.liebiao_pic);

                gridview_id.setSelector(new ColorDrawable(Color.TRANSPARENT));

                for(int i=0;i<parent.getCount();i++){
                    View v=parent.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        view.setBackgroundResource(R.mipmap.tupian_p);
                    } else {
                        if( v != null)  //在当前页面不会进行刷新，需要自己手动设置背景隐藏
                            v.setBackgroundResource(0);
                    }
                }
                //点击就发送消息
                BaseApp.current_pic_play_num = position;
                Message pic_msg = myHandler.obtainMessage(Contents.IMAGE_ITEM_CLICK);//30
                pic_msg.arg1 = BaseApp.current_pic_play_num;
                myHandler.sendMessage(pic_msg);
            }
        });

        musicFragment = new MusicFragment();
        videoFragment = new VideoFragment();
        pictureFragment = new PictureFragment();

        fragments.add(musicFragment);
        fragments.add(videoFragment);
        fragments.add(pictureFragment);
        addFragmentLayout();
        myHandler = new MyHandler();

        if (BaseApp.iffirststart){
            BaseApp.iffirststart = false;
            SharedPreferences sharedPreferences = getSharedPreferences("DongfengDataSave", Activity.MODE_PRIVATE);
            last_music_id = sharedPreferences.getString("MUSICID", "0");
            list_size_first = sharedPreferences.getInt("LISTSIZE", 0);
            position_first = sharedPreferences.getInt("POSITION", 0);
            musicplaymode_first =  sharedPreferences.getInt("MUSICPLAYMODE", 0);
            if(BaseApp.ifdebug) {
                System.out.println(TAG+"-onCreate-"+"last music ----" + last_music_id + "-----" + list_size_first + "-----" + position_first);
            }
            if (last_music_id.equals("0")) {
                BaseApp.current_music_play_num = 0;
            } else {
                mp3Info_first = MediaUtils.getMp3Info(this, Integer.parseInt(last_music_id));
                if (mp3Info_first != null) {
                    if(BaseApp.ifdebug) {
                        System.out.println(TAG+"-onCreate-"+"mp3info---" + mp3Info_first.toString());
                    }
                    //只要找到后，我就去发消息，用来显示，可能会显示不正确
                    Message msg = myHandler.obtainMessage(Contents.MUSIC_NO_CHANGE);//113
                    myHandler.sendMessage(msg);
                }
            }
        }
//        mp3Infos = MediaUtils.getMp3Infos(this);
//        mp4Infos = Mp4MediaUtils.getMp4Infos(this);
        //这里不能这样加载，程序的卡顿不说，还会出现内存溢出的情况
//        picInfos = PicMediaUtils.getPicInfos(this);
        new MyAsyncTask().execute();
        Intent intent = new Intent(this, PlayMusicService.class);
        startService(intent); //启动服务
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseApp.exitUI = false;
        if(BaseApp.current_fragment == 2){
            //改变底栏//防止按返回键后，重新进入，底栏显示不正确
            button_layout.setBackgroundResource(R.mipmap.dilan_pic);
            button_fangda.setVisibility(View.VISIBLE);
            button_suoxiao.setVisibility(View.VISIBLE);
            button_play_mode.setVisibility(View.GONE);
            button_shangqu.setImageResource(R.mipmap.shangqu_pic);
            button_bofang.setImageResource(R.mipmap.zanting_pic);
            button_xiaqu.setImageResource(R.mipmap.xiaqu_pic);
            button_liebiao.setImageResource(R.mipmap.liebiao_pic);
        }

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        myHandler.sendEmptyMessage(Contents.VIDEO_COME_BACK);//23
        bindPlayMusicService();
    }

    @Override
        protected void onPause() {
            super.onPause();
            unbindPlayMusicService();
        }

        @Override
        protected void onStop() {
            super.onStop();
        BaseApp.exitUI = true;
    }

    @Override
    public void onPicLieBiaoClose() {
        leibieliebiao.setVisibility(View.GONE);
    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case Contents.MUSIC_PROGRESS://1
                    musicFragment.seekBar1.setProgress(msg.arg1);
                    musicFragment.song_current_time.setText(MediaUtils.formatTime(msg.arg1));
                    break;
                case Contents.MUSIC_LOAD_FINISH://12
                    if (BaseApp.ifopenliebiao == 1 && BaseApp.current_media == 0) {
                        if(BaseApp.ifdebug){
                            System.out.println(TAG+"-MyHandler-"+"mp3Infos is OK,come to update the list...");
                        }
                        //数据获取结束准备刷新
                        if (!BaseApp.ifMusicLoaded) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        }else if (mp3Infos == null || mp3Infos.size() == 0) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setImageResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        } else {
                            loading_layout.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            musicvideolist.setVisibility(View.VISIBLE);

                            if (mymusiclistviewAdapter != null) {
                                mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                                musicvideolist.setAdapter(mymusiclistviewAdapter);
                            } else {
                                mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                                musicvideolist.setAdapter(mymusiclistviewAdapter);
                            }

                            if(BaseApp.current_music_play_num < 0){
//                                    musicvideolist.setFocusable(true);
//                                    musicvideolist.setFocusableInTouchMode(true);
//                                    musicvideolist.requestFocus();
                                    musicvideolist.setSelection(0);
                            } else{
//                                    musicvideolist.setFocusable(true);
//                                    musicvideolist.setFocusableInTouchMode(true);
//                                    musicvideolist.requestFocus();
                                    musicvideolist.setSelection(BaseApp.current_music_play_num);
                            }
                            mymusiclistviewAdapter.notifyDataSetChanged();
                        }
                    }
                    break;

                case Contents.MUSIC_NO_CHANGE://113
                    //当数量一致时，默认为没有改变列表，这里可能存在bug，但是没办法
                    int nums_temp = MediaUtils.getMp3Nums(MainActivity.this);
                    if(nums_temp!=0 && position_first < nums_temp && nums_temp == list_size_first){
                        BaseApp.current_music_play_num = position_first;
                        Bitmap albumBitmap2 = MediaUtils.getArtwork(MainActivity.this, mp3Info_first.getId(), mp3Info_first.getAlbumId(), true, false);
                        musicFragment.album_icon.setImageBitmap(albumBitmap2);
                        musicFragment.song_name.setText(mp3Info_first.getTittle());
                        musicFragment.zhuanji_name.setText(mp3Info_first.getAlbum());
                        musicFragment.chuangzhe_name.setText(mp3Info_first.getArtist());//
                        musicFragment.num_order.setText((position_first+1) + "/" + list_size_first);
                        musicFragment.song_total_time.setText(MediaUtils.formatTime(mp3Info_first.getDuration()));
                        musicFragment.seekBar1.setProgress(0);
                        musicFragment.seekBar1.setMax((int) mp3Info_first.getDuration());

                        button_play_mode.setImageResource(music_play_mode_resource[musicplaymode_first]);
                        musicFragment.changeMusicPlayModeUI(musicplaymode_first);
                        BaseApp.music_play_mode = musicplaymode_first;
                    }else{
                        BaseApp.current_music_play_num = 0;
                    }
                    break;
                case Contents.CHANGE_FRAGMENT_MUSCI_PLAY_MODE:
                    if(BaseApp.ifdebug){
                        System.out.println("MainActivity-handlmessage-BaseApp.music_play_mode"+BaseApp.music_play_mode);
                    }
                    playMusicService.setPlay_mode(BaseApp.music_play_mode);

                    break;
                case Contents.MUSIC_REFRESH_INFO_UI://2
                    Mp3Info mp3Info = new Mp3Info();
                    mp3Info = playMusicService.mp3Infos.get(BaseApp.current_music_play_num);
                    Bitmap albumBitmap = MediaUtils.getArtwork(getApplicationContext(), mp3Info.getId(), mp3Info.getAlbumId(), true, false);
                    musicFragment.album_icon.setImageBitmap(albumBitmap);
                    musicFragment.song_name.setText(mp3Info.getTittle());
                    musicFragment.zhuanji_name.setText(mp3Info.getAlbum());
                    musicFragment.chuangzhe_name.setText(mp3Info.getArtist());
                    musicFragment.num_order.setText((BaseApp.current_music_play_num + 1) + "/" + playMusicService.mp3Infos.size());
                    musicFragment.song_total_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
                    musicFragment.seekBar1.setProgress(0);
                    musicFragment.seekBar1.setMax((int) mp3Info.getDuration());

                    if (playMusicService.isPlaying()) {
                        button_bofang.setImageResource(R.mipmap.bofang);
                    } else {
                        button_bofang.setImageResource(R.mipmap.zanting);
                    }
                    break;
                case Contents.VIDEO_LOAD_FINISH://22
                    if (BaseApp.ifopenliebiao == 1 && BaseApp.current_media == 1) {
                        if(BaseApp.ifdebug){
                            System.out.println(TAG+"-MyHandler-"+"mp4Infos is OK,come to update the list...");
                        }
                        //数据获取结束准备刷新
                        if (!BaseApp.ifVideoLoaded) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        } else if (mp4Infos == null || mp4Infos.size() == 0) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        } else {
                            loading_layout.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            musicvideolist.setVisibility(View.VISIBLE);

                            if (myvideolistviewAdapter != null) {
                                myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                                musicvideolist.setAdapter(myvideolistviewAdapter);
                            } else {
                                myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                                musicvideolist.setAdapter(myvideolistviewAdapter);
                            }
                            if(BaseApp.current_video_play_num < 0){
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(0);
                            } else{
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(BaseApp.current_music_play_num);
                            }
                            myvideolistviewAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case Contents.VIDEO_COME_BACK://用户按下Home键返回来之后，需要自动继续播放 23
                    if(BaseApp.current_fragment ==0 && musicFragment!= null){
                        button_play_mode.setImageResource(music_play_mode_resource[BaseApp.music_play_mode]);
                        musicFragment.changeMusicPlayModeUI(BaseApp.music_play_mode);
                    }
                    if(BaseApp.current_fragment ==1 && videoFragment != null && mp4Infos!=null && mp4Infos.get(BaseApp.current_video_play_num)!=null){
                        button_play_mode.setImageResource(music_play_mode_resource[BaseApp.video_play_mode]);
                        button_bofang.setImageResource(R.mipmap.bofang);
                        videoFragment.play_video(mp4Infos.get(BaseApp.current_video_play_num).getData());
                        if(videoFragment.ispause){
                            button_bofang.setImageResource(R.mipmap.zanting);
                            videoFragment.pause();
                        }
                    }
                    break;
                case Contents.IMAGE_ITEM_CLICK:  //接收点击消息  30
                    //显示页面进行控制
                    if(BaseApp.ifdebug){
                        System.out.println(TAG+"-MyHandler-"+"enter the picture show...");
                    }
                    BaseApp.current_pic_play_num = msg.arg1;

//                    if (BaseApp.current_fragment == 0) {
//                        playMusicService.pause();
//                    }

                    if (BaseApp.current_fragment != 2) {
                        if(BaseApp.ifdebug){
                            System.out.println(TAG+"-MyHandler-"+"create new picture fragment...");
                        }
                        pictureFragment = (PictureFragment) fragments.get(2);
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        fragments.get(BaseApp.current_fragment).onStop();//停止当前的fragment

                        if (pictureFragment.isAdded())   //判断PICfragment是否在栈中
                            pictureFragment.onStart();
                        else {
                            ft.add(R.id.media_fragment, pictureFragment);
                            ft.commit();
                        }
                        ft.hide(fragments.get(BaseApp.current_fragment));   //隐藏
                        ft.remove(fragments.get(BaseApp.current_fragment));
                        ft.show(pictureFragment);
                        BaseApp.current_fragment = 2;
                        if(BaseApp.ifdebug){
                            System.out.println(TAG+"-MyHandler-"+"current picture num " + BaseApp.current_pic_play_num);
                        }
                        pictureFragment.setImageShow(BaseApp.current_pic_play_num);
                    } else {
                        if(BaseApp.ifdebug){
                            System.out.println("current picture num " + BaseApp.current_pic_play_num);
                        }
                        pictureFragment.changeImageShow(BaseApp.current_pic_play_num);
                    }
                    //这里加入这个的话，图片的加载会变得很慢，但是不加入刷新，如何让选中背景颜色改变呢？
//                    myGridViewAdapter2.notifyDataSetChanged();
                    break;
                case Contents.IMAGE_PPT_COMEBACK://处理PPTactivity返回来的消息显示   31
                    if(BaseApp.ifdebug){
                        System.out.println(TAG+"-MyHandler-"+"get message to pause...");
                    }
                    String mybigPicPath = MainActivity.picInfos.get(BaseApp.current_pic_play_num).getData();
//                    Bitmap bm = pictureFragment.GetLocalOrNetBitmap("file://" + mybigPicPath);
                    Bitmap bm = pictureFragment.convertToBitmap(mybigPicPath, 800, 350);
                    pictureFragment.big_pic_show.setImageBitmap(bm);
                    button_bofang.setImageResource(R.mipmap.zanting_pic);
                    break;
                case Contents.IMAGE_LOAD_FINISH://32
                    if (BaseApp.ifopenliebiao == 1 && BaseApp.current_media == 2){

                        if (!BaseApp.ifPicloaded) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        } else if (picInfos == null || picInfos.size() == 0) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        }else{
                            loading_layout.setVisibility(View.GONE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.VISIBLE);

                            if(myGridViewAdapter2 != null){
                                myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                                gridview_id.setAdapter(myGridViewAdapter2);
                            }else{
                                if(BaseApp.ifdebug){
                                    System.out.println(TAG+"-MyHandler-"+"picInfos is OK,come to update the gridview...");
                                }
                                myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                                gridview_id.setAdapter(myGridViewAdapter2);
//                                myGridViewAdapter2.notifyDataSetChanged();
                            }
                            if(BaseApp.current_pic_play_num < 0){
//                                    gridview_id.setFocusable(true);
//                                    gridview_id.setFocusableInTouchMode(true);
//                                    gridview_id.requestFocus();
                                    gridview_id.setSelection(0);

                            } else{
//                                    gridview_id.setFocusable(true);
//                                    gridview_id.setFocusableInTouchMode(true);
//                                    gridview_id.requestFocus();
                                    gridview_id.setSelection(BaseApp.current_pic_play_num);
                            }
                            myGridViewAdapter2.notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void stop(int isstop) {
        if(isstop == 1){
            button_bofang.setImageResource(R.mipmap.zanting);
        }
    }

    @Override
    public void publish(int progress) {
        if(BaseApp.current_fragment == 0){
//            musicFragment.seekBar1.setProgress(progress);//如果直接处理，在video界面退出后，返回music，出现秒退现象
            Message msg = myHandler.obtainMessage(Contents.MUSIC_PROGRESS);//1
            msg.arg1 = progress;
            myHandler.sendMessage(msg);
        }
    }

    @Override
    public void change(int position) {
        if(mp3Info_first!=null){
            mp3Info_first = null;
        }else {
            if (playMusicService.mp3Infos.size() > 0) {
                BaseApp.current_music_play_num = position;
                SharedPreferences sharedPreferences= getSharedPreferences("DongfengDataSave", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(BaseApp.ifdebug){
                    System.out.println(TAG+"-change-"+"set info to sharepreference...");
                }
                editor.putString("MUSICID", String.valueOf(playMusicService.mp3Infos.get(position).getId()));
                editor.putInt("LISTSIZE", playMusicService.mp3Infos.size());
                editor.putInt("POSITION",BaseApp.current_music_play_num);
                editor.apply();

                if (BaseApp.ifopenliebiao == 1) {
                    mymusiclistviewAdapter.notifyDataSetChanged();
                }
                if (BaseApp.current_fragment == 0) {
                    //不能再回调函数中处理，从video界面退出，再进入，切到music界面会出现闪退现象
                    Message msg = myHandler.obtainMessage(Contents.MUSIC_REFRESH_INFO_UI);//2
                    myHandler.sendMessage(msg);
                }
            }
        }
    }

    private void addFragmentLayout() {
        if(BaseApp.ifdebug) {
            System.out.println(TAG+"-addFragmentLayout-"+"current_fragment is:------" + BaseApp.current_fragment);
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(fragments.get(BaseApp.current_fragment).isAdded()){
            fragments.get(BaseApp.current_fragment).onStart();
        }else{
            ft.add(R.id.media_fragment, fragments.get(BaseApp.current_fragment));
            ft.commit();
        }
    }

    @Override
    public void onClick(View v) {

        switch(BaseApp.current_media){
            case 0:
                button_music.setBackgroundResource(R.mipmap.liebiao_p);
                break;
            case 1:
                button_video.setBackgroundResource(R.mipmap.liebiao_p);
                break;
            case 2:
                button_pic.setBackgroundResource(R.mipmap.liebiao_p);
                break;
        }

        switch (v.getId()){
            case R.id.button_shangqu:
                BaseApp.ifopenliebiao = 0;
                leibieliebiao.setVisibility(View.GONE);
                if(BaseApp.current_fragment == 0 && mp3Infos != null && mp3Infos.size() > 0) {
                    playMusicService.prev();
                }else if(BaseApp.current_fragment == 1 && mp4Infos != null && mp4Infos.size() > 0){
                    videoFragment.playVideopre();
                    button_bofang.setImageResource(R.mipmap.bofang);
                }else if(BaseApp.current_fragment == 2 && picInfos != null && picInfos.size() > 0) {
                        pictureFragment.playPicpre();
                }
                break;
            case R.id.button_bofang:
                BaseApp.ifopenliebiao = 0;
                leibieliebiao.setVisibility(View.GONE);
                if(BaseApp.current_fragment == 0 && mp3Infos != null && mp3Infos.size() > 0){
                    if(playMusicService.isPlaying()){
                        button_bofang.setImageResource(R.mipmap.zanting);
                        playMusicService.pause();
                    }else{
                        if(playMusicService.isPause()){
                            button_bofang.setImageResource(R.mipmap.bofang);
                            playMusicService.start();
                        }else{
                            playMusicService.play(BaseApp.current_music_play_num);
                        }
                    }
                }else if(BaseApp.current_fragment == 1&& mp4Infos != null && mp4Infos.size() > 0){
                    if(videoFragment.isPlaying()){
                        if(BaseApp.ifdebug){
                            System.out.println(TAG+"-onClick-"+"----------1111-----------");
                        }
                        button_bofang.setImageResource(R.mipmap.zanting);   //三角形
                        videoFragment.pause();
                    }else{
                        if(videoFragment.ispause){
                            if(BaseApp.ifdebug) {
                                System.out.println(TAG+"-onClick-"+"----------2222-----------");
                            }
                            button_bofang.setImageResource(R.mipmap.bofang);
                            videoFragment.start();
                        }else{
                            if(BaseApp.ifdebug) {
                                System.out.println(TAG+"-onClick-"+"----------3333-----------");
                            }
                            button_bofang.setImageResource(R.mipmap.bofang);
                            videoFragment.play_video(mp4Infos.get(BaseApp.current_video_play_num).getData());
                        }
                    }
                }else if(BaseApp.current_fragment == 2 && picInfos != null && picInfos.size() > 0){
                    button_bofang.setImageResource(R.mipmap.bofang_pic);
                    //开启幻灯片Activity
                    Intent intent = new Intent(MainActivity.this,PPTActivity.class);
                    startActivityForResult(intent,0);
                }
                break;
            case R.id.button_xiaqu:
                BaseApp.ifopenliebiao = 0;
                leibieliebiao.setVisibility(View.GONE);
                if(BaseApp.current_fragment == 0 && mp3Infos != null && mp3Infos.size() > 0){
                    playMusicService.next();
                }else if(BaseApp.current_fragment == 1&& mp4Infos != null && mp4Infos.size() > 0){
                    videoFragment.playVideonext();
                    button_bofang.setImageResource(R.mipmap.bofang);
                }else if(BaseApp.current_fragment == 2 && picInfos != null && picInfos.size() > 0) {
                    pictureFragment.playPicnext();
                }
                break;
            case R.id.button_play_mode:
                BaseApp.ifopenliebiao = 0;
                leibieliebiao.setVisibility(View.GONE);
                if(BaseApp.current_fragment == 0 && mp3Infos != null && mp3Infos.size() > 0) {
                    BaseApp.music_play_mode++;
                    if (BaseApp.music_play_mode >= 4) {
                        BaseApp.music_play_mode = 0;
                    }

                    SharedPreferences sharedPreferences= getSharedPreferences("DongfengDataSave", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //  System.out.println("set info to sharepreference...");
                    editor.putInt("MUSICPLAYMODE",BaseApp.music_play_mode);
                    editor.apply();

                    button_play_mode.setImageResource(music_play_mode_resource[BaseApp.music_play_mode]);
                    musicFragment.changeMusicPlayModeUI(BaseApp.music_play_mode);
                    playMusicService.setPlay_mode(BaseApp.music_play_mode);
                }else if(BaseApp.current_fragment == 1 && mp4Infos != null && mp4Infos.size() > 0) {

                }
                    break;
            case R.id.button_fangda:
                if(picInfos != null && picInfos.size() > 0) {
                    pictureFragment.pic_play_fangda();
                }
                break;
            case R.id.button_suoxiao:
                if(picInfos != null && picInfos.size() > 0) {
                    pictureFragment.pic_play_suoxiao();
                }
                break;
            case R.id.button_liebiao:
                if(BaseApp.ifopenliebiao == 0) {
                    BaseApp.ifopenliebiao =1;
                    leibieliebiao.setVisibility(View.VISIBLE);

                    if(BaseApp.current_media == 0) {  //打开的是音乐列表
                  //      mp3Infos = MediaUtils.getMp3Infos(this);
                        if (!BaseApp.ifMusicLoaded) {
                            loading_layout.setVisibility(View.VISIBLE);

                            musicvideolist.setVisibility(View.GONE);

                            gridview_id.setVisibility(View.GONE);

                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        }else if (mp3Infos == null || mp3Infos.size() == 0) {
                            loading_layout.setVisibility(View.VISIBLE);

                            musicvideolist.setVisibility(View.GONE);

                            gridview_id.setVisibility(View.GONE);
                            frame_image.setImageResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        } else {
                            loading_layout.setVisibility(View.GONE);

                            gridview_id.setVisibility(View.GONE);

                            musicvideolist.setVisibility(View.VISIBLE);
                         //
                            if (mymusiclistviewAdapter != null) {
                                mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                                musicvideolist.setAdapter(mymusiclistviewAdapter);
                            } else {
                                mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                                musicvideolist.setAdapter(mymusiclistviewAdapter);
                            }
                            if(BaseApp.current_music_play_num<0){
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(0);
                            } else{
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(BaseApp.current_music_play_num);
                            }
                            mymusiclistviewAdapter.notifyDataSetChanged();
                        }
                    }else if(BaseApp.current_media == 1){   //打开视频列表
                        if (!BaseApp.ifVideoLoaded) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        } else if (mp4Infos == null || mp4Infos.size() == 0) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
//                            frame_image.setImageResource(R.mipmap.jinggao_ico);
                            frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        } else {
                            loading_layout.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            musicvideolist.setVisibility(View.VISIBLE);
                         //   musicvideolist.requestFocusFromTouch();
                            if (myvideolistviewAdapter != null) {
                                myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                                musicvideolist.setAdapter(myvideolistviewAdapter);
                            } else {
                                myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                                musicvideolist.setAdapter(myvideolistviewAdapter);
                            }
                            if(BaseApp.current_video_play_num < 0){
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(0);
                            } else{
//                                musicvideolist.setFocusable(true);
//                                musicvideolist.setFocusableInTouchMode(true);
//                                musicvideolist.requestFocus();
                                musicvideolist.setSelection(BaseApp.current_music_play_num);
                            }
                            myvideolistviewAdapter.notifyDataSetChanged();
                        }

                    }else if(BaseApp.current_media == 2){
                        if (!BaseApp.ifPicloaded) {
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.drawable.loading_ico);
                            frameAnim = (AnimationDrawable) frame_image.getBackground();
                            frameAnim.start();
                            no_music_resource.setText("加载中");
                        } else if (picInfos == null || picInfos.size() == 0) {
                            if(frameAnim != null)
                                frameAnim.stop();
                            loading_layout.setVisibility(View.VISIBLE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.GONE);
                            frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                            no_music_resource.setText("无文件");
                        }else{
                            if(frameAnim != null)
                                frameAnim.stop();
                            loading_layout.setVisibility(View.GONE);
                            musicvideolist.setVisibility(View.GONE);
                            gridview_id.setVisibility(View.VISIBLE);
                            if(myGridViewAdapter2 != null){
                                myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                                gridview_id.setAdapter(myGridViewAdapter2);
                            }else{
                                if(BaseApp.ifdebug){
                                    System.out.println(TAG+"-onClick-"+"picInfos is OK,come to update the gridview...");
                                }
                                myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                                gridview_id.setAdapter(myGridViewAdapter2);
                            }
                            if(BaseApp.current_pic_play_num < 0){
//                                gridview_id.setFocusable(true);
//                                gridview_id.setFocusableInTouchMode(true);
//                                gridview_id.requestFocus();
                                gridview_id.setSelection(0);
                            } else{
//                                gridview_id.setFocusable(true);
//                                gridview_id.setFocusableInTouchMode(true);
//                                gridview_id.requestFocus();
                                gridview_id.setSelection(BaseApp.current_pic_play_num);
                            }
                            myGridViewAdapter2.notifyDataSetChanged();
                        }
                    }
                }else{
                    BaseApp.ifopenliebiao = 0;
                    leibieliebiao.setVisibility(View.GONE);
                }
                break;
            case R.id.button_music:
                BaseApp.current_media = 0;
                button_music.setBackgroundResource(R.mipmap.liebiao_p);
                button_video.setBackground(null);
                button_pic.setBackground(null);

                if (!BaseApp.ifMusicLoaded) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    frame_image.setBackgroundResource(R.drawable.loading_ico);
                    frameAnim = (AnimationDrawable) frame_image.getBackground();
                    frameAnim.start();
                    no_music_resource.setText("加载中");
                }else if (mp3Infos == null || mp3Infos.size() == 0) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    frame_image.setImageResource(R.mipmap.jinggao_ico);
                    no_music_resource.setText("无文件");
                } else {
                    loading_layout.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    musicvideolist.setVisibility(View.VISIBLE);
                //    musicvideolist.requestFocusFromTouch();
                    if (mymusiclistviewAdapter != null) {
                        mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                        musicvideolist.setAdapter(mymusiclistviewAdapter);
                    } else {
                        mymusiclistviewAdapter = new MymusiclistviewAdapter(MainActivity.this, mp3Infos);
                        musicvideolist.setAdapter(mymusiclistviewAdapter);
                    }

                    if(BaseApp.current_music_play_num<0){
//                        musicvideolist.setFocusable(true);
//                        musicvideolist.setFocusableInTouchMode(true);
//                        musicvideolist.requestFocus();
                        musicvideolist.setSelection(0);
                    } else{
//                        musicvideolist.setFocusable(true);
//                        musicvideolist.setFocusableInTouchMode(true);
//                        musicvideolist.requestFocus();
                        musicvideolist.setSelection(BaseApp.current_music_play_num);
                    }
                    mymusiclistviewAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.button_video:
                BaseApp.current_media = 1;
                button_video.setBackgroundResource(R.mipmap.liebiao_p);
                button_music.setBackground(null);
                button_pic.setBackground(null);

                if (!BaseApp.ifVideoLoaded) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    frame_image.setBackgroundResource(R.drawable.loading_ico);
                    frameAnim = (AnimationDrawable) frame_image.getBackground();
                    frameAnim.start();
                    no_music_resource.setText("加载中");
                } else if (mp4Infos == null || mp4Infos.size() == 0) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
//                            frame_image.setImageResource(R.mipmap.jinggao_ico);
                    frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                    no_music_resource.setText("无文件");
                } else {
                    loading_layout.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    musicvideolist.setVisibility(View.VISIBLE);
                 //   musicvideolist.requestFocusFromTouch();
                    if (myvideolistviewAdapter != null) {
                        myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                        musicvideolist.setAdapter(myvideolistviewAdapter);
                    } else {
                        myvideolistviewAdapter = new MyvideolistviewAdapter(MainActivity.this, mp4Infos);
                        musicvideolist.setAdapter(myvideolistviewAdapter);
                    }

                    if(BaseApp.current_video_play_num < 0){
//                        musicvideolist.setFocusable(true);
//                        musicvideolist.setFocusableInTouchMode(true);
//                        musicvideolist.requestFocus();
                        musicvideolist.setSelection(0);
                    } else{
//                        musicvideolist.setFocusable(true);
//                        musicvideolist.setFocusableInTouchMode(true);
//                        musicvideolist.requestFocus();
                        musicvideolist.setSelection(BaseApp.current_music_play_num);
                    }
                    myvideolistviewAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.button_pic:
                BaseApp.current_media = 2;
                button_pic.setBackgroundResource(R.mipmap.liebiao_p);
                button_music.setBackground(null);
                button_video.setBackground(null);

                if (!BaseApp.ifPicloaded) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
                    frame_image.setBackgroundResource(R.drawable.loading_ico);
                    frameAnim = (AnimationDrawable) frame_image.getBackground();
                    frameAnim.start();
                    no_music_resource.setText("加载中");
                } else if (picInfos == null || picInfos.size() == 0) {
                    loading_layout.setVisibility(View.VISIBLE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.GONE);
//                            frame_image.setImageResource(R.mipmap.jinggao_ico);
                    frame_image.setBackgroundResource(R.mipmap.jinggao_ico);
                    no_music_resource.setText("无文件");
                }else{
                    loading_layout.setVisibility(View.GONE);
                    musicvideolist.setVisibility(View.GONE);
                    gridview_id.setVisibility(View.VISIBLE);
                    if(myGridViewAdapter2 != null){
                        myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                        gridview_id.setAdapter(myGridViewAdapter2);
                    }else{
                        System.out.println("picInfos is OK,come to update the gridview...");
                        myGridViewAdapter2 = new MyGridViewAdapter2(MainActivity.this,picInfos);
                        gridview_id.setAdapter(myGridViewAdapter2);
                    }
                    if(BaseApp.current_pic_play_num < 0){
//                        gridview_id.setFocusable(true);
//                        gridview_id.setFocusableInTouchMode(true);
//                        gridview_id.requestFocus();
                        gridview_id.setSelection(0);
                    } else{
//                        gridview_id.setFocusable(true);
//                        gridview_id.setFocusableInTouchMode(true);
//                        gridview_id.requestFocus();
                        gridview_id.setSelection(BaseApp.current_pic_play_num);
                    }
                    myGridViewAdapter2.notifyDataSetChanged();
                }
                break;
        }
    }

    //点击之后改变颜色
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //在current_media是2的情况下，list列表就会被隐藏了。所以不可能跳转
        //改变底栏
        button_layout.setBackgroundResource(R.mipmap.dilan);
        button_fangda.setVisibility(View.GONE);
        button_suoxiao.setVisibility(View.GONE);
        button_play_mode.setVisibility(View.VISIBLE);
        button_shangqu.setImageResource(R.mipmap.shangqu);
        button_bofang.setImageResource(R.mipmap.bofang);
        button_xiaqu.setImageResource(R.mipmap.xiaqu);
        button_liebiao.setImageResource(R.mipmap.liebiao);

        if(BaseApp.current_media == 0){

            button_play_mode.setImageResource(music_play_mode_resource[BaseApp.music_play_mode]);
            BaseApp.current_music_play_num = position;
            mymusiclistviewAdapter.notifyDataSetChanged();
            if(BaseApp.current_fragment != 0) {
                if(BaseApp.ifdebug) {
                    System.out.println(TAG+"-onItemClick-"+"返回到音乐界面");
                }
                musicFragment = (MusicFragment) fragments.get(0);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                fragments.get(BaseApp.current_fragment).onStop();//停止当前的fragment

                if(musicFragment.isAdded()){
                    musicFragment.onStart();
                }else{
                    ft.add(R.id.media_fragment, musicFragment);
                    ft.commit();
                }
                ft.hide(fragments.get(BaseApp.current_fragment));
                ft.remove(fragments.get(BaseApp.current_fragment));
                ft.show(musicFragment);
                BaseApp.current_fragment = 0;
                BaseApp.isfirststartmusic = 0;//去除第一次启动的接口回调,这样可以重新初始化返回的音乐界面
            }
            if(BaseApp.ifdebug) {
                System.out.println("MainActivity-onItemClick-music_play_mode:" + BaseApp.music_play_mode);
            }
            playMusicService.setPlay_mode(BaseApp.music_play_mode);
            playMusicService.play(BaseApp.current_music_play_num);
            myHandler.sendEmptyMessage(Contents.CHANGE_FRAGMENT_MUSCI_PLAY_MODE);

        }else if(BaseApp.current_media == 1){

            button_play_mode.setImageResource(music_play_mode_resource[BaseApp.video_play_mode]);

            if(BaseApp.ifdebug) {
                System.out.println(TAG+"-onItemClick-"+"enter the video play...");
            }
            BaseApp.current_video_play_num = position;
            myvideolistviewAdapter.notifyDataSetChanged();
            if(BaseApp.current_fragment == 0){
                playMusicService.pause();
            }
            if(BaseApp.current_fragment != 1) {
                if(BaseApp.ifdebug){
                    System.out.println(TAG+"-onItemClick-"+"create new videofragment...");
                }
                videoFragment = (VideoFragment) fragments.get(1);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                fragments.get(BaseApp.current_fragment).onStop();//停止当前的fragment
                if(videoFragment.isAdded())   //判断videofragment是否在栈中
                    videoFragment.onStart();
                else{
                    ft.add(R.id.media_fragment, videoFragment);
                    ft.commit();
                }
                ft.hide(fragments.get(BaseApp.current_fragment));   //隐藏music
                ft.remove(fragments.get(BaseApp.current_fragment));
                ft.show(videoFragment);
                BaseApp.current_fragment = 1;
                System.out.println(mp4Infos.get(position).getData());
                button_bofang.setImageResource(R.mipmap.bofang);
                videoFragment.playVideoFromMainactivity(position,0);
            }else{//如果就是在视频这个界面,这种做法貌似行不通
                if(BaseApp.ifdebug) {
                    System.out.println(TAG+"-onItemClick-"+"Already in videofragment...");
                }
                videoFragment.playVideoFromUser(position);
            }
        }
    }

    @Override
    public void onUIChange(int position) {
        Mp3Info mp3Info = new Mp3Info();
        if(mp3Infos.size()>0) {
            BaseApp.current_music_play_num = position;
            if (BaseApp.ifopenliebiao == 1) {
                mymusiclistviewAdapter.notifyDataSetChanged();
            }
            mp3Info = mp3Infos.get(BaseApp.current_music_play_num);
            Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
            musicFragment.album_icon.setImageBitmap(albumBitmap);
            musicFragment.song_name.setText(mp3Info.getTittle());
            musicFragment.zhuanji_name.setText(mp3Info.getAlbum());
            musicFragment.chuangzhe_name.setText(mp3Info.getArtist());//
            musicFragment.changeMusicPlayModeUI(BaseApp.music_play_mode);
            musicFragment.num_order.setText((BaseApp.current_music_play_num + 1) + "/" + mp3Infos.size());
            musicFragment.song_total_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
            musicFragment.seekBar1.setProgress(0);
            musicFragment.seekBar1.setMax((int) mp3Info.getDuration());
        }
    }
    @Override
    public void onServiceCommand(int i) {
        switch(i){
            case 1:
                playMusicService.seek(BaseApp.current_music_play_progress);
                break;
            case 2:
                playMusicService.pause();
                button_bofang.setImageResource(R.mipmap.zanting);
                break;
            case 3:
                playMusicService.start();
                button_bofang.setImageResource(R.mipmap.bofang);
                break;
        }
    }

    @Override
    public void onLieBiaoClose() {
        leibieliebiao.setVisibility(View.GONE);
    }

    @Override
    public void onVideoProgressSave() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Mp4Info> mp4Infos_temp = new ArrayList<>();
                try {
                    mp4Infos_temp = BaseApp.dbUtils.findAll(Mp4Info.class);
                    if(mp4Infos_temp == null || mp4Infos_temp.size() ==0){
                        //直接添加
                        if(mp4Infos!=null && mp4Infos.size()>0){
                            for(int i=0;i<mp4Infos.size();i++){
                                BaseApp.dbUtils.save(mp4Infos.get(i));
                            }
                        }
                    }else{
                        BaseApp.dbUtils.deleteAll(Mp4Info.class);
                        if(mp4Infos!=null && mp4Infos.size()>0){
                            for(int i=0;i<mp4Infos.size();i++){
                                BaseApp.dbUtils.save(mp4Infos.get(i));
                            }
                        }
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onVideoStateChange() {
        //如果不加入判断，导致第一从视频切过来，播放按钮过大。显示出bug
        if(BaseApp.current_media == 2 && BaseApp.current_fragment == 2){

        }else {
            button_bofang.setImageResource(R.mipmap.zanting);
        }
    }

    @Override
    public void onVideoScreenChange(int progress) {
        //改变屏幕大小
        if(BaseApp.ifFullScreenState) {

            if (BaseApp.statebarheight == 0) {
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                BaseApp.statebarheight = frame.top;   //获取状态栏的高度
                if (BaseApp.ifdebug) {
                    System.out.println("Mainactivity-onVideoScreenChange-statebarheight" + BaseApp.statebarheight);
                }
            }
            if (BaseApp.dibuheight == 0) {
                BaseApp.dibuheight = button_layout.getHeight();  //获取底栏的高度

                if (BaseApp.ifdebug) {
                    System.out.println("Mainactivity-onVideoScreenChange-dibuheight" + BaseApp.dibuheight);
                }
        }

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mRLayout.setSystemUiVisibility(View.INVISIBLE);
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            button_layout.setVisibility(View.GONE);

            RelativeLayout.LayoutParams mFramlayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            mFramlayout.setMargins(0, 0, 0, 0);
            frame_content.setLayoutParams(mFramlayout);
        }else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
            mRLayout.setSystemUiVisibility(View.VISIBLE);
            MainActivity.this.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mRLayout.setFocusable(true);
            button_layout.setVisibility(View.VISIBLE);

            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            if(BaseApp.ifdebug) {
                System.out.println(TAG+"-onVideoScreenChange-"+"状态栏的高度2:----" + statusBarHeight);
            }

            //系统默认去掉了标题栏，只是保留了状态栏，状态栏的高度是63dp，但是返回后获取的高度为0
            if(statusBarHeight == 0) {
                RelativeLayout.LayoutParams mFramlayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                mFramlayout.setMargins(0,BaseApp.statebarheight,0,BaseApp.dibuheight);
                frame_content.setLayoutParams(mFramlayout);
            }
        }
    }

    @Override
    public void onVideoLieBiaoClose() {
        leibieliebiao.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(BaseApp.ifdebug) {
            System.out.println(TAG+"-onBackPressed-"+"enter back press...");
        }
        if(BaseApp.ifopenliebiao ==1){
            BaseApp.ifopenliebiao = 0;
            leibieliebiao.setVisibility(View.GONE);
        }
        if(BaseApp.current_fragment == 0 && playMusicService.isPlaying()){
            button_bofang.setImageResource(R.mipmap.bofang);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     //
        switch (resultCode){
        case RESULT_OK:
            if(BaseApp.ifdebug) {
                System.out.println(TAG+"-onActivityResult-"+"pic come back to mainactivity...");
            }
            Message pic_result_msg = new Message();
            pic_result_msg.what = Contents.IMAGE_PPT_COMEBACK;//31
           if(BaseApp.current_pic_play_num == picInfos.size() - 1 ){
            }else{
               //每次退出时，都往前突一个，显得不是很好
                BaseApp.current_pic_play_num = BaseApp.current_pic_play_num - 1;
            }
            myHandler.sendMessage(pic_result_msg);
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class MyAsyncTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {

            mp3Infos = MediaUtils.getMp3Infos(MainActivity.this);
            BaseApp.ifMusicLoaded = true;
            myHandler.sendEmptyMessage(Contents.MUSIC_LOAD_FINISH);//12

            mp4Infos = Mp4MediaUtils.getMp4Infos(MainActivity.this);
            BaseApp.ifVideoLoaded = true;
            //加载数据库文件
            if(BaseApp.ifdebug) {
                System.out.println(TAG+"-MyAsyncTask-"+"加载视频完成");
            }
            List<Mp4Info> mp4Infos_temp = new ArrayList<>();
            try {
                mp4Infos_temp = BaseApp.dbUtils.findAll(Mp4Info.class);
                if(mp4Infos_temp != null && mp4Infos_temp.size() >0){
                    for(int i=0; i<mp4Infos.size();i++){
                        for(int j = 0;j < mp4Infos_temp.size();j++){
                            if(mp4Infos.get(i).getDisplay_name().equals(mp4Infos_temp.get(j).getDisplay_name())){
                                if(BaseApp.ifdebug) {
                                    System.out.println(TAG+"-MyAsyncTask-"+"find same video...");
                                }
                                mp4Infos.get(i).setVideo_item_progressed(mp4Infos_temp.get(j).getVideo_item_progressed());
                            }
                        }
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
            myHandler.sendEmptyMessage(Contents.VIDEO_LOAD_FINISH);//22

            picInfos = PicMediaUtils.getPicInfos(MainActivity.this);
            BaseApp.ifPicloaded = true;
            myHandler.sendEmptyMessage(Contents.IMAGE_LOAD_FINISH);//32

            if(BaseApp.ifdebug && picInfos!=null && picInfos.size()>0){
                System.out.println("图片路径:"+picInfos.get(0).getData());
            }
            return null;
        }

        //当doInBackground方法返回后被调用
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BaseApp.ifdebug) {
            System.out.println(TAG+"-onDestroy-"+"enter destroy...");
        }
    }
}
