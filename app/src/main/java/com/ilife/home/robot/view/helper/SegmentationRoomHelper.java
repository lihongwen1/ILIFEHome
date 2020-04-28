package com.ilife.home.robot.view.helper;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;

import com.ilife.home.robot.R;
import com.ilife.home.robot.bean.Coordinate;
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

public class SegmentationRoomHelper {
    private static final String TAG = "VirtualWallHelper";
    private List<VirtualWallBean> vwBeans;
    private MapView mMapView;
    private VirtualWallHelper.VWOT vwot = VirtualWallHelper.VWOT.ADD;
    private static final int MIN_WALL_LENGTH = 60;
    private PointF downPoint;
    private RectF curVw;//当前正在操作的虚拟墙
    private Path vwPath;
    private int selectVwNum = -1;
    private final int ICON_RADIUS = 50;
    private VirtualWallBean curVwBean;//当前操作虚拟墙对象
    private Matrix mMatrix;
    private Matrix mBoundaryMatix;
    private int BOUNDARY_LENGTH = 40;
    private int curMaxNumber;//当前最大的虚拟墙序号
    private Path gatePath = new Path();

    public enum VWOT {
        NOON(21),
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


    public SegmentationRoomHelper(MapView mapView) {
        vwBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.vwPath = new Path();
        this.downPoint = new PointF();
        this.curVw = new RectF();
        this.mMatrix = new Matrix();
        this.mBoundaryMatix = new Matrix();
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
        mMatrix.reset();
        for (VirtualWallBean vw : vwBeans) {
            if (vw.getPullIcon() != null && vw.getPullIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                vwot = VirtualWallHelper.VWOT.PULL;
                Log.d(TAG, "拉伸虚拟墙");
                curVwBean = vw;
                break;
            } else if (vw.getDeleteIcon() != null && vw.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                vwot = VirtualWallHelper.VWOT.DELETE;
                Log.d(TAG, "删除虚拟墙");
                curVwBean = vw;
                break;
            } else if (vw.getBoundaryRegion() != null && vw.getBoundaryRegion().contains(Math.round(mapX), Math.round(mapY))) {//drag
                Log.d(TAG, "推拽虚拟墙");
                selectVwNum = vw.getNumber();
                vwot = VirtualWallHelper.VWOT.DRAG;
                curVwBean = vw;
                break;
            }
        }
        if (vwot == VirtualWallHelper.VWOT.ADD && getUsefulWallNum() >= 10) {
            ToastUtils.showToast(UiUtil.getString(R.string.map_aty_max_count));
            vwot = VirtualWallHelper.VWOT.NOON;
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
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curVwBean.getPointCoordinate()));
                    updateVirtualWall();
                }
                break;
            case PULL:
                float[] orginalCoo = curVwBean.getPointCoordinate();
                orginalCoo[2] = mMapView.reMatrixCoordinateX(mapX);
                orginalCoo[3] = mMapView.reMatrixCoordinateY(mapY);
                updateVirtualWall();
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
                curVw.setEmpty();
                if (getUsefulWallNum() < 10 && DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_WALL_LENGTH) {
                    float[] coordinate = new float[]{mMapView.reMatrixCoordinateX(downPoint.x), mMapView.reMatrixCoordinateY(downPoint.y), mMapView.reMatrixCoordinateX(mapX), mMapView.reMatrixCoordinateY(mapY)};
                    makeLeftToRight(coordinate);
                    VirtualWallBean virtualWallBean = new VirtualWallBean(curMaxNumber++, -1, coordinate, 2);
                    selectVwNum = virtualWallBean.getNumber();
                    vwBeans.add(virtualWallBean);
                    updateVirtualWall();
                    if (tooCloseToChargePort(virtualWallBean)) {
                        ToastUtils.showVirFbdCloseTip(virtualWallBean.getType(), 1);
                    }
                    if (tooCloseToRobotPosition(virtualWallBean)) {
                        ToastUtils.showVirFbdCloseTip(virtualWallBean.getType(), 0);
                    }
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
                            updateVirtualWall();
                        }
                    }
                }
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离 预览地图偏移量
                if (curVwBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curVwBean.getPointCoordinate()));
                    updateVirtualWall();
                }
                float ctx = mMapView.reMatrixCoordinateX(mapX) - mMapView.reMatrixCoordinateX(downPoint.x);//x轴平移距离
                float cty = mMapView.reMatrixCoordinateY(mapY) - mMapView.reMatrixCoordinateY(downPoint.y);//y轴平移距离 主机坐标偏移量
                mMatrix.reset();
                mMatrix.postTranslate(ctx, cty);
                curVwBean.updateCoordinateWithMatrix(mMatrix);
                if (tooCloseToChargePort(curVwBean)) {
                    ToastUtils.showVirFbdCloseTip(curVwBean.getType(), 1);
                }
                if (tooCloseToRobotPosition(curVwBean)) {
                    ToastUtils.showVirFbdCloseTip(curVwBean.getType(), 0);
                }
                break;
            case PULL:
                if (tooCloseToChargePort(curVwBean)) {
                    ToastUtils.showVirFbdCloseTip(curVwBean.getType(), 1);
                }
                if (tooCloseToRobotPosition(curVwBean)) {
                    ToastUtils.showVirFbdCloseTip(curVwBean.getType(), 0);
                }
                break;
        }
        curVwBean = null;
        mMatrix.reset();
        curVw.setEmpty();
        vwot = VirtualWallHelper.VWOT.ADD;
    }


    /**
     * 查询到服务其电子墙数据后调用绘制电子墙
     * virtual wall encode data,need parse to virtual wall bean
     *
     * @param vwStr 服务器电子墙数据集合
     */
    public void setVirtualWall(String vwStr) {
        if (!TextUtils.isEmpty(vwStr)) {
            byte[] bytes = Base64.decode(vwStr, Base64.DEFAULT);
            int vwCounts = bytes.length / 12;//一条虚拟墙含12个字节，4个保留字节，加2个坐标（x,y）
            vwBeans.clear();
            int sx, sy, ex, ey;
            VirtualWallBean vwBean;
            for (int i = 0; i < vwCounts; i++) {
                sx = DataUtils.bytesToInt(bytes[12 * i + 4], bytes[12 * i + 5]);
                sy = -DataUtils.bytesToInt(bytes[12 * i + 6], bytes[12 * i + 7]);
                ex = DataUtils.bytesToInt(bytes[12 * i + 8], bytes[12 * i + 9]);
                ey = -DataUtils.bytesToInt(bytes[12 * i + 10], bytes[12 * i + 11]);
                vwBean = new VirtualWallBean(curMaxNumber++, -1, new float[]{sx, sy, ex, ey}, 1);
                makeLeftToRight(vwBean.getPointCoordinate());
                vwBeans.add(vwBean);
            }
        } else {
            vwBeans.clear();
        }
        updateVirtualWall();
    }

    private boolean tooCloseToChargePort(VirtualWallBean bean) {
        if (mMapView.getStandPointF() == null) {
            return false;
        }
        return bean.getBoundaryRegion().contains((int) mMapView.matrixCoordinateX(mMapView.getStandPointF().x),
                (int) mMapView.matrixCoordinateY(mMapView.getStandPointF().y));
    }

    private boolean tooCloseToRobotPosition(VirtualWallBean bean) {
        if (mMapView.getEndX() == mMapView.getEndY() && mMapView.getEndX() == 0) {
            return false;
        }
        return bean.getBoundaryRegion().contains((int) mMapView.getEndX(),
                (int) mMapView.getEndY());
    }

    /**
     * 绘制电子墙
     * 根据最新的缩放比例刷新虚拟墙
     */
    public void updateVirtualWall() {
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
                    mMatrix.mapPoints(coordinate);
                }
                Path llPath = new Path();
                llPath.moveTo(coordinate[0], coordinate[1]);
                llPath.lineTo(coordinate[2], coordinate[3]);
                vwPath.moveTo(coordinate[0], coordinate[1]);
                vwPath.lineTo(coordinate[2], coordinate[3]);
                /**
                 * 绘制虚拟墙边界
                 */
                mBoundaryMatix.reset();
                boundaryPath.reset();
                float[] cooBoundary = new float[8];
                float k = (coordinate[3] - coordinate[1]) / (coordinate[2]
                        - coordinate[0]);
                float degree = (float) (Math.atan(k) * 180 / Math.PI);
                mBoundaryMatix.setTranslate(-coordinate[0], -coordinate[1]);
                mBoundaryMatix.postRotate(-degree, 0, 0);

                mBoundaryMatix.mapPoints(coordinate);
                int x_direction = coordinate[0] < coordinate[2] ? 1 : -1;
                cooBoundary[0] = coordinate[0] - x_direction * BOUNDARY_LENGTH;
                cooBoundary[1] = coordinate[1] - x_direction * BOUNDARY_LENGTH;
                cooBoundary[2] = coordinate[0] - x_direction * BOUNDARY_LENGTH;
                cooBoundary[3] = coordinate[1] + x_direction * BOUNDARY_LENGTH;
                cooBoundary[4] = coordinate[2] + x_direction * BOUNDARY_LENGTH;
                cooBoundary[5] = coordinate[3] + x_direction * BOUNDARY_LENGTH;
                cooBoundary[6] = coordinate[2] + x_direction * BOUNDARY_LENGTH;
                cooBoundary[7] = coordinate[3] - x_direction * BOUNDARY_LENGTH;
                mBoundaryMatix.invert(mBoundaryMatix);
                mBoundaryMatix.mapPoints(cooBoundary);

                float minx = cooBoundary[0], miny = cooBoundary[1], maxx = cooBoundary[0], maxy = cooBoundary[1];
                for (int i = 0; i < cooBoundary.length; i++) {
                    float value = cooBoundary[i];
                    if (i % 2 == 0) {//x
                        if (value < minx) {
                            minx = value;
                        }
                        if (value > maxx) {
                            maxx = value;
                        }
                    } else {//y
                        if (value < miny) {
                            miny = value;
                        }
                        if (value > maxy) {
                            maxy = value;
                        }
                    }
                }
                boundaryRegion = new Region((int) minx, (int) miny, (int) maxx, (int) maxy);
                boundaryPath.moveTo(cooBoundary[0], cooBoundary[1]);
                boundaryPath.lineTo(cooBoundary[2], cooBoundary[3]);
                boundaryPath.lineTo(cooBoundary[4], cooBoundary[5]);
                boundaryPath.lineTo(cooBoundary[6], cooBoundary[7]);
                boundaryPath.close();
                vir.setDeleteIcon(new RectF(cooBoundary[0] - ICON_RADIUS, cooBoundary[1] - ICON_RADIUS, cooBoundary[0] + ICON_RADIUS, cooBoundary[1] + ICON_RADIUS));
                vir.setPullIcon(new RectF(cooBoundary[4] - ICON_RADIUS, cooBoundary[5] - ICON_RADIUS, cooBoundary[4] + ICON_RADIUS, cooBoundary[5] + ICON_RADIUS));
                boundaryRegion.setPath(boundaryPath, boundaryRegion);
                vir.setBoundaryRegion(boundaryRegion);
                vir.setBoundaryPath(boundaryPath);
                coordinate = new float[]{mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[1])
                        , mMapView.matrixCoordinateX(vir.getPointCoordinate()[2]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[3])};
                if (selectVwNum == vir.getNumber()) {
                    List<Coordinate> containsCoo = new ArrayList<>();
                    for (Coordinate coo : mMapView.getPointList()) {
                        if (coo.getType() == 2) {
                            if (boundaryRegion.contains((int) mMapView.matrixCoordinateX(coo.getX()), (int) mMapView.matrixCoordinateY(coo.getY()))) {
                                double space = DataUtils.lineToPointSpace(coordinate[0], coordinate[1], coordinate[2], coordinate[3],
                                        mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
                                MyLogger.d(TAG, "点线距离：    " + space);
                                if (space < 10) {
                                    containsCoo.add(coo);
                                }

                            }
                        }

                    }
                    float mind1 = -1, mind2 = -1;
                    for (Coordinate coo : containsCoo) {
                        float dis1 = DataUtils.distance(coordinate[0], coordinate[1], mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
                        float dis2 = DataUtils.distance(coordinate[2], coordinate[3], mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
                        if (mind1 == -1 && mind2 == -1) {
                            mind1 = dis1;
                            mind2 = dis2;
                        } else {
                            if (dis1 < mind1) {
                                mind1 = dis1;
                            }
                            if (dis2 < mind2) {
                                mind2 = dis2;
                            }
                        }
                    }
                    gatePath.reset();
                    PathMeasure pathMeasure = new PathMeasure();
                    pathMeasure.setPath(llPath, false);
                    pathMeasure.getSegment(mind1, pathMeasure.getLength() - mind2, gatePath, true);
                }
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
        updateVirtualWall();
    }

    public boolean isClose() {
        boolean isTooCloseToChargePort = false;
        boolean isTooCloseToRobot = false;
        for (VirtualWallBean bean : vwBeans) {
            if (bean.getState() == 3) {
                continue;
            }
            if (tooCloseToChargePort(bean)) {
                isTooCloseToChargePort = true;
                break;
            }
            if (tooCloseToRobotPosition(bean)) {
                isTooCloseToRobot = true;
                break;
            }
        }
        boolean isClose = isTooCloseToChargePort || isTooCloseToRobot;
        if (isClose) {
            ToastUtils.showVirFbdCloseTip(-1, isTooCloseToRobot ? 0 : 1);
        }
        return isClose;
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
                    coor = Math.round(coordinate[i]);
                } else {
                    coor = Math.round(-coordinate[i]);
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

    /**
     * 检查
     */
    private void makeLeftToRight(float[] coordinate) {
        if (coordinate[0] > coordinate[2]) {
            float sx = coordinate[0];
            float sy = coordinate[1];
            coordinate[0] = coordinate[2];
            coordinate[1] = coordinate[3];
            coordinate[2] = sx;
            coordinate[3] = sy;
        }
    }

    public Path getGatePath() {
        return gatePath;
    }
}
