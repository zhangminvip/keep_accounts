package com.winorout.cashbook.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winorout.cashbook.R;
import com.winorout.cashbook.accounting.AccountingActivity;
import com.winorout.cashbook.qmyan.login.LoginActivity;
import com.winorout.cashbook.widget.Draws;
import com.winorout.cashbook.wxwan.Target;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnClickListener {

    private LinearLayout user;
    private Boolean isLogin;
    private TextView userNameText;

    private Draws settarget; //月预算设置
    private ImageView imageView; //记账图标
    private ImageView head;   //登录点击处
    private ImageView more;  //扩展功能
    private static int i = 0; //判断当前是Content还是Menu，0表示Content，1表示Menu。
    private int titlekey = 0; //判断当前标题栏应显示状况
    private TextView title; //标题
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    /**
     * 滚动显示和隐藏menu时，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * 屏幕宽度值。
     */
    private int screenWidth;

    /**
     * menu最多可以滑动到的左边缘。值由menu布局的宽度来定，marginLeft到达此值之后，不能再减少。
     */
    private int leftEdge;

    /**
     * menu最多可以滑动到的右边缘。值恒为0，即marginLeft到达0之后，不能增加。
     */
    private int rightEdge = 0;

    /**
     * menu完全显示时，留给content的宽度值。
     */
    private int menuPadding = 80;

    /**
     * 主内容的布局。
     */
    private View content;

    /**
     * menu的布局。
     */
    private View menu;

    /**
     * menu布局的参数，通过此参数来更改leftMargin的值。
     */
    private LinearLayout.LayoutParams menuParams;

    /**
     * 记录手指按下时的横坐标。
     */
    private float xDown;

    /**
     * 记录手指移动时的横坐标。
     */
    private float xMove;

    /**
     * 记录手机抬起时的横坐标。
     */
    private float xUp;

    /**
     * menu当前是显示还是隐藏。只有完全显示或隐藏menu时才会更改此值，滑动过程中此值无效。
     */
    private boolean isMenuVisible;

    /**
     * 用于计算手指滑动的速度。
     */
    private VelocityTracker mVelocityTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences("loginUser", Context.MODE_PRIVATE);//临时存储一些数据
        editor = mSharedPreferences.edit();
        initValues();
        if (mSharedPreferences.getInt("key", titlekey) == 0) {
            title.setText("天天记账");
        } else {
            title.setText("账户余额");
        }
        initEvent();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isLogin = mSharedPreferences.getBoolean("isLogin", false);
        if (isLogin) {
            userNameText.setText(mSharedPreferences.getString("userName", "用户名"));
        } else {
            userNameText.setText("点击登录");
        }
    }

    private void initEvent() {
    /*
    * 设置几个监听事件*/
        imageView.setOnClickListener(this);
        head.setOnClickListener(this);
        more.setOnClickListener(this);
        content.setOnTouchListener(this);
        title.setOnClickListener(this);
        user.setOnClickListener(this);
        settarget.setOnClickListener(this);
    }

    /**
     * 初始化一些关键性数据。包括获取屏幕的宽度，给content布局重新设置宽度，给menu布局重新设置宽度和偏移距离等。
     */
    private void initValues() {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenWidth = window.getDefaultDisplay().getWidth();
        content = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
        // 将menu的宽度设置为屏幕宽度减去menuPadding并减去8分之一界面宽度
        menuParams.width = screenWidth - menuPadding - screenWidth/8;
        // 左边缘的值赋值为menu宽度的负数
        leftEdge = -menuParams.width;
        // menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
        menuParams.leftMargin = leftEdge;
        // 将content的宽度设置为屏幕宽度
        content.getLayoutParams().width = screenWidth;

        imageView = (ImageView) findViewById(R.id.pic);
        head = (ImageView) findViewById(R.id.head);
        more = (ImageView) findViewById(R.id.more);
        title = (TextView) findViewById(R.id.title);

        user = (LinearLayout) findViewById(R.id.user);
        userNameText = (TextView) findViewById(R.id.user_name);

        settarget = (Draws)findViewById(R.id.settarget);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pic:
                Intent intent = new Intent(MainActivity.this, AccountingActivity.class);
                startActivity(intent);
                break;
            case R.id.more:
                Toast.makeText(this, "功能正在完善中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.head:
                if (i == 0) {
                    scrollToMenu();
                    i = 1;
                } else {
                    scrollToContent();
                    i = 0;
                }
                break;
            case R.id.title:
                if (mSharedPreferences.getInt("key", titlekey) == 0) {
                    title.setText("账户余额");
                    titlekey = 1;
                } else {
                    title.setText("天天记账");
                    titlekey = 0;
                }
                editor.putInt("key", titlekey);
                editor.commit();
                break;
            case R.id.user:
                if (isLogin) {
                    Toast.makeText(this, "打开个人中心，待完善。。。", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
                break;
            case R.id.settarget:
                Intent intent1 = new Intent(MainActivity.this, Target.class);
                startActivity(intent1);
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时，记录按下时的横坐标
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整menu的leftMargin值，从而显示和隐藏menu
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown);
                if (isMenuVisible) {
                    menuParams.leftMargin = distanceX;
                } else {
                    menuParams.leftMargin = leftEdge + distanceX;
                }
                if (menuParams.leftMargin < leftEdge) {
                    menuParams.leftMargin = leftEdge;
                } else if (menuParams.leftMargin > rightEdge) {
                    menuParams.leftMargin = rightEdge;
                }
                menu.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                // 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
                xUp = event.getRawX();
                if (wantToShowMenu()) {
                    if (shouldScrollToMenu()) {
                        scrollToMenu();
                    } else {
                        scrollToContent();
                    }
                } else if (wantToShowContent()) {
                    if (shouldScrollToContent()) {
                        scrollToContent();
                    } else {
                        scrollToMenu();
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    /**
     * 判断当前手势的意图是不是想显示content。如果手指移动的距离是负数，且当前menu是可见的，则认为当前手势是想要显示content。
     *
     * @return 当前手势想显示content返回true，否则返回false。
     */
    private boolean wantToShowContent() {
        return xUp - xDown < 0 && isMenuVisible;
    }

    /**
     * 判断当前手势的意图是不是想显示menu。如果手指移动的距离是正数，且当前menu是不可见的，则认为当前手势是想要显示menu。
     *
     * @return 当前手势想显示menu返回true，否则返回false。
     */
    private boolean wantToShowMenu() {
        return xUp - xDown > 0 && !isMenuVisible;
    }

    /**
     * 判断是否应该滚动将menu展示出来。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，
     * 就认为应该滚动将menu展示出来。
     *
     * @return 如果应该滚动将menu展示出来返回true，否则返回false。
     */
    private boolean shouldScrollToMenu() {
        return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * 判断是否应该滚动将content展示出来。如果手指移动距离加上menuPadding大于屏幕的1/2，
     * 或者手指移动速度大于SNAP_VELOCITY， 就认为应该滚动将content展示出来。
     *
     * @return 如果应该滚动将content展示出来返回true，否则返回false。
     */
    private boolean shouldScrollToContent() {
        return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }

    /**
     * 将屏幕滚动到menu界面，滚动速度设定为30.
     */
    private void scrollToMenu() {
        new ScrollTask().execute(30);
    }

    /**
     * 将屏幕滚动到content界面，滚动速度设定为-30.
     */
    private void scrollToContent() {
        new ScrollTask().execute(-30);
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event content界面的滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int j = 0;
            int leftMargin = menuParams.leftMargin;
            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
            while (true) {
                leftMargin = leftMargin + speed[0];
                if (leftMargin > rightEdge) {
                    leftMargin = rightEdge;
                    break;
                }
                if (leftMargin < leftEdge) {
                    leftMargin = leftEdge;
                    break;
                }
                publishProgress(leftMargin);
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
                j += 1;
                if (j == 3) {
                    sleep(1);
                    j = 0;
                }
            }
            if (speed[0] > 0) {
                isMenuVisible = true;
            } else {
                isMenuVisible = false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            menuParams.leftMargin = leftMargin[0];
            menu.setLayoutParams(menuParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            menuParams.leftMargin = leftMargin;
            menu.setLayoutParams(menuParams);
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param millis 指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
