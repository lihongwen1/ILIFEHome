package com.ilife.home.robot.view.helper;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;

import com.ilife.home.robot.R;
import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VirtualWallHelper {
    private static final String TAG = "VirtualWallHelper";
    private List<VirtualWallBean> vwBeans;
    private MapView mMapView;
    private VWOT vwot = VWOT.ADD;
    private static final int MIN_WALL_LENGTH = 60;
    private PointF downPoint;
    private RectF curVw;//当前正在操作的虚拟墙
    private Path vwPath;
    private int leftX, leftY;
    private int selectVwNum = -1;
    private final int ICON_RADIUS = 50;
    private VirtualWallBean curVwBean;//当前操作虚拟墙对象
    private Matrix mMatix;

    public enum VWOT {
        ADD(22),
        DELETE(23),
        DRAG(24),
        PULL(26);

        final int nativeType;

        VWOT(int type) {
            this.nativeType = type;
        }
    }

    public List<VirtualWallBean> getVwBeans() {
        return vwBeans;
    }

    public RectF getCurVw() {
        return curVw;
    }

    public Path getVwPath() {
        return vwPath;
    }


    public VirtualWallHelper(MapView mapView) {
        vwBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.vwPath = new Path();
        this.downPoint = new PointF();
        this.curVw = new RectF();
        this.mMatix = new Matrix();
    }

    /**
     * @param event 触摸事件
     * @param mapX  屏幕坐标转化后的地图坐标X
     * @param mapY  屏幕坐标转化后的地图坐标Y
     */
    public void onTouch(MotionEvent event, float mapX, float mapY) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                doOnActionDown(mapX, mapY);
                break;
            case MotionEvent.ACTION_MOVE:
                doOnActionMove(mapX, mapY);
                break;
            case MotionEvent.ACTION_UP:
                doOnActionUp(mapX, mapY);
                break;
        }
        mMapView.invalidateUI();
    }

    private void doOnActionDown(float mapX, float mapY) {
        downPoint.set(mapX, mapY);
        mMatix.reset();
        for (VirtualWallBean vw : vwBeans) {
            if (vw.getBoundaryRegion() != null && vw.getBoundaryRegion().contains(Math.round(mapX), Math.round(mapY))) {//drag
                Log.d(TAG, "推拽虚拟墙");
                selectVwNum = vw.getNumber();
                vwot = VWOT.DRAG;
                curVwBean = vw;
            } else if (vw.getPullIcon() != null && vw.getPullIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                vwot = VWOT.PULL;
                Log.d(TAG, "拉伸虚拟墙");
                curVwBean = vw;
            } else if (vw.getDeleteIcon() != null && vw.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                vwot = VWOT.DELETE;
                Log.d(TAG, "删除虚拟墙");
                curVwBean = vw;
            }
        }

    }

    private void doOnActionMove(float mapX, float mapY) {
        switch (vwot) {
            case ADD:
                if (getUsefulWallNum() < 10) {
                    if (DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_WALL_LENGTH) {
                        curVw.set(downPoint.x, downPoint.y, mapX, mapY);
                    }
                }
                break;
            case DELETE:
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离
                if (curVwBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatix.reset();
                    mMatix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curVwBean.getPointCoordinate()));
                    drawVirtualWall();
                }
                break;
            case PULL:
                float[] coordinate = new float[]{mMapView.matrixCoordinateX(curVwBean.getPointCoordinate()[0]), mMapView.matrixCoordinateY(curVwBean.getPointCoordinate()[1])
                        , mMapView.matrixCoordinateX(curVwBean.getPointCoordinate()[2]), mMapView.matrixCoordinateY(curVwBean.getPointCoordinate()[3])};

                float k = (coordinate[3] - coordinate[1]) / (coordinate[2]
                        - coordinate[0]);
                //
                //垂直偏离需要的坐标变化
                float translationY = (float) (60 * (Math.sqrt(1 + k * k) / (1 + k * k)));
                float translationX = Math.abs(k) * translationY;
                //坐标延长
                float lengthenX = (float) (60 * Math.abs(Math.sqrt(1 / (k * k + 1))));
                float lengthenY = lengthenX * k;
                float endX, endY;
                //TODO k=0;
                if (k < 0) {//lengthY<0
                    endX = mapX + translationX + lengthenX;
                    endY = mapY + translationY + lengthenY;
                } else {
                    endX = mapX + translationX - lengthenX;
                    endY = mapY - translationY - lengthenY;
                }
                float[] orginalCoo = curVwBean.getPointCoordinate();
                orginalCoo[2] = mMapView.reMatrixCoordinateX(endX);
                orginalCoo[3] = mMapView.reMatrixCoordinateY(endY);
                drawVirtualWall();
                break;
            default:
                break;
        }
    }

    /**
     * @param mapX
     * @param mapY
     */
    private void doOnActionUp(float mapX, float mapY) {
        switch (vwot) {
            case ADD:
                if (downPoint.x > mapX) {//保证从左往右绘制
                    float tempX = mapX;
                    float tempY = mapY;
                    mapX = downPoint.x;
                    mapY = downPoint.y;
                    downPoint.set(tempX, tempY);
                }
                //clear the cur wall rect ,and make it to a virtual wall bean
                curVw.setEmpty();
                if (getUsefulWallNum() < 10 && DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_WALL_LENGTH) {
                    VirtualWallBean virtualWallBean = new VirtualWallBean(vwBeans.size() + 1, -1,
                            new float[]{mMapView.reMatrixCoordinateX(downPoint.x), mMapView.reMatrixCoordinateY(downPoint.y), mMapView.reMatrixCoordinateX(mapX), mMapView.reMatrixCoordinateY(mapY)}
                            , 2);
                    selectVwNum = virtualWallBean.getNumber();
                    vwBeans.add(virtualWallBean);
                    drawVirtualWall();
                }
                break;
            case DELETE:
                if ((mapX == downPoint.x && mapY == downPoint.y) || DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) < 10) {//点击事件
                    if (curVwBean != null) {
                        if (curVwBean.getDeleteIcon() != null && curVwBean.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了删除键
                            if (curVwBean.getState() == 2) {//新增的电子墙，还未保存到服务器，可以直接移除
                                vwBeans.remove(curVwBean);
                            }
                            if (curVwBean.getState() == 1) {//服务器上的电子墙，可能操作会被取消掉，只需要改变状态
                                curVwBean.setState(3);
                                curVwBean.clear();
                            }
                            selectVwNum = -1;
                            drawVirtualWall();
                        }
                    }
                }
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离 预览地图偏移量
                if (curVwBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatix.reset();
                    mMatix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curVwBean.getPointCoordinate()));
                    drawVirtualWall();
                }
                float ctx = mMapView.reMatrixCoordinateX(mapX) - mMapView.reMatrixCoordinateX(downPoint.x);//x轴平移距离
                float cty = mMapView.reMatrixCoordinateY(mapY) - mMapView.reMatrixCoordinateY(downPoint.y);//y轴平移距离 主机坐标偏移量
                mMatix.reset();
                mMatix.postTranslate(ctx, cty);
                curVwBean.updateCoordinateWithMatrix(mMatix);
                break;
            case PULL:

                break;
        }
        curVwBean = null;
        mMatix.reset();
        curVw.setEmpty();
        vwot = VWOT.ADD;
    }


    /**
     * 查询到服务其电子墙数据后调用绘制电子墙
     * virtual wall encode data,need parse to virtual wall bean
     *
     * @param vwStr 服务器电子墙数据集合
     */
    public void drawVirtualWall(String vwStr, int leftX, int leftY) {
        this.leftX = leftX;
        this.leftY = leftY;
        if (!TextUtils.isEmpty(vwStr)) {
            byte[] bytes = Base64.decode(vwStr, Base64.DEFAULT);
            int vwCounts = bytes.length / 12;//一条虚拟墙含12个字节，4个保留字节，加2个坐标（x,y）
            vwBeans.clear();
            int sx, sy, ex, ey;
            VirtualWallBean vwBean;
            for (int i = 0; i < vwCounts; i++) {
                sx = DataUtils.bytesToInt(bytes[12 * i + 4], bytes[12 * i + 5]) - leftX;
                sy = leftY - DataUtils.bytesToInt(bytes[12 * i + 6], bytes[12 * i + 7]);
                ex = DataUtils.bytesToInt(bytes[12 * i + 8], bytes[12 * i + 9]) - leftX;
                ey = leftY - DataUtils.bytesToInt(bytes[12 * i + 10], bytes[12 * i + 11]);

                if (sx > ex) {//保证始终是从左往右绘制
                    int tempX = ex;
                    int tempY = ey;
                    ex = sx;
                    ey = sy;
                    sx = tempX;
                    sy = tempY;
                }
                vwBean = new VirtualWallBean(i + 1, -1, new float[]{sx, sy, ex, ey}, 1);
                vwBeans.add(vwBean);
            }
        }

        drawVirtualWall();
    }

    /**
     * 绘制电子墙
     */
    public void drawVirtualWall() {
        if (vwBeans == null) {
            return;
        }
        vwPath.reset();
        Path boundaryPath = new Path();
        Region boundaryRegion;
        float[] coordinate;
        for (VirtualWallBean vir : vwBeans) {
            if (vir.getState() != 3) {
                coordinate = new float[]{mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[1])
                        , mMapView.matrixCoordinateX(vir.getPointCoordinate()[2]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[3])};
                if (selectVwNum == vir.getNumber()) {
                    mMatix.mapPoints(coordinate);
                }
                vwPath.moveTo(coordinate[0], coordinate[1]);
                vwPath.lineTo(coordinate[2], coordinate[3]);
                /**
                 * 绘制虚拟墙边界
                 */
                boundaryPath.reset();
                float k = (coordinate[3] - coordinate[1]) / (coordinate[2]
                        - coordinate[0]);
                //
                if (selectVwNum == vir.getNumber()) {
                    MyLogger.d(TAG, "虚拟墙斜率：" + k + "坐标：  " + Arrays.toString(vir.getPointCoordinate()));
                }
                //垂直偏离需要的坐标变化
                float translationY = (float) (60 * (Math.sqrt(1 + k * k) / (1 + k * k)));
                float translationX = Math.abs(k) * translationY;
                //坐标延长
                float lengthenX = (float) (60 * Math.abs(Math.sqrt(1 / (k * k + 1))));
                float lengthenY = lengthenX * k;
                //TODO 处理k为1 k为0的情况
                //绘制虚拟墙的矩形边界
                float[] cooBoundary = new float[8];//虚拟墙边界坐标数组
                if (k < 0) {// lengthY<0
                    if (coordinate[0] < coordinate[2]) {
                        boundaryRegion = new Region((int) coordinate[0], (int) coordinate[3], (int) coordinate[2], (int) coordinate[1]);
                        cooBoundary[0] = coordinate[0] - lengthenX - translationX;
                        cooBoundary[1] = coordinate[1] - lengthenY - translationY;

                        cooBoundary[2] = coordinate[2] + lengthenX - translationX;
                        cooBoundary[3] = coordinate[3] + lengthenY - translationY;

                        cooBoundary[4] = coordinate[2] + lengthenX + translationX;
                        cooBoundary[5] = coordinate[3] + lengthenY + translationY;

                        cooBoundary[6] = coordinate[0] - lengthenX + translationX;
                        cooBoundary[7] = coordinate[1] - lengthenY + translationY;
                    } else {
                        boundaryRegion = new Region((int) coordinate[2], (int) coordinate[1], (int) coordinate[0], (int) coordinate[3]);
                        cooBoundary[0] = coordinate[0] - lengthenX - translationX;
                        cooBoundary[1] = coordinate[1] - lengthenY - translationY;

                        cooBoundary[2] = coordinate[2] + lengthenX - translationX;
                        cooBoundary[3] = coordinate[3] + lengthenY - translationY;

                        cooBoundary[4] = coordinate[2] + lengthenX + translationX;
                        cooBoundary[5] = coordinate[3] + lengthenY + translationY;

                        cooBoundary[6] = coordinate[0] - lengthenX + translationX;
                        cooBoundary[7] = coordinate[1] - lengthenY + translationY;
                    }
                } else {//lengthY>0
                    boundaryRegion = new Region((int) coordinate[0], (int) coordinate[1], (int) coordinate[2], (int) coordinate[3]);
                    cooBoundary[0] = coordinate[0] - lengthenX + translationX;
                    cooBoundary[1] = coordinate[1] - lengthenY - translationY;

                    cooBoundary[2] = coordinate[2] + lengthenX + translationX;
                    cooBoundary[3] = coordinate[3] + lengthenY - translationY;

                    cooBoundary[4] = coordinate[2] + lengthenX - translationX;
                    cooBoundary[5] = coordinate[3] + lengthenY + translationY;


                    cooBoundary[6] = coordinate[0] - lengthenX - translationX;
                    cooBoundary[7] = coordinate[1] - lengthenY + translationY;

                }
                boundaryPath.moveTo(cooBoundary[0], cooBoundary[1]);
                boundaryPath.lineTo(cooBoundary[2], cooBoundary[3]);
                boundaryPath.lineTo(cooBoundary[4], cooBoundary[5]);
                boundaryPath.lineTo(cooBoundary[6], cooBoundary[7]);
                boundaryPath.close();
                vir.setDeleteIcon(new RectF(cooBoundary[0] - ICON_RADIUS, cooBoundary[1] - ICON_RADIUS, cooBoundary[0] + ICON_RADIUS, cooBoundary[1] + ICON_RADIUS));
                vir.setPullIcon(new RectF(cooBoundary[4] - ICON_RADIUS, cooBoundary[5] - ICON_RADIUS, cooBoundary[4] + ICON_RADIUS, cooBoundary[5] + ICON_RADIUS));

                vir.setBoundaryPath(boundaryPath);

                boundaryRegion.setPath(boundaryPath, boundaryRegion);
                vir.setBoundaryRegion(boundaryRegion);
            }
        }
    }


    /**
     * 撤销所有电子墙操作，恢复到与服务器数据一致的状态
     */
    public void undoAllOperation() {
        if (vwBeans != null && vwBeans.size() > 0) {
            Iterator<VirtualWallBean> iterator = vwBeans.iterator();
            while (iterator.hasNext()) {
                VirtualWallBean virtualWallBean = iterator.next();
                if (virtualWallBean.getState() == 2) {
                    iterator.remove();
                } else if (virtualWallBean.getState() == 3) {//被置为待删除的服务器电子墙恢复状态
                    virtualWallBean.setState(1);
                }
            }
        }
        drawVirtualWall();
    }


    /**
     * 获取电子墙列表,包含新增，和删除
     *
     * @return
     */
    public String getVwData() {
        List<VirtualWallBean> usefulVr = new ArrayList<>();
        for (VirtualWallBean vr : vwBeans) {
            if (vr.getState() != 3) {
                usefulVr.add(vr);
            }
        }
        byte[] bData = new byte[usefulVr.size() * 12];
        byte[] intToByte;
        int index = 0;
        float[] coordinate;
        int coor;
        for (VirtualWallBean vr : usefulVr) {
            coordinate = vr.getPointCoordinate();
            index += 4;
            for (int i = 0; i < coordinate.length; i++) {
                if (i % 2 == 0) {
                    coor = Math.round(coordinate[i] + leftX);
                } else {
                    coor = Math.round(leftY - coordinate[i]);
                }
                intToByte = DataUtils.intToBytes(coor);
                bData[index] = intToByte[0];
                index++;
                bData[index] = intToByte[1];
                index++;
            }
        }
        return Base64.encodeToString(bData, Base64.NO_WRAP);
    }


    /**
     * the method which will be used to deal with virtual wall data
     */

    private int getUsefulWallNum() {
        int num = 0;
        for (VirtualWallBean vb : vwBeans) {
            if (vb.getState() == 1 || vb.getState() == 2) {
                num++;
            }
        }
        return num;
    }

    public int getSelectVwNum() {
        return selectVwNum;
    }
}
