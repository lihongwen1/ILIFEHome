package com.ilife.home.robot.base;

import com.ilife.home.robot.R;

import butterknife.OnClick;

/**
 * back activity
 */
public abstract  class BackBaseActivity  <T extends BasePresenter>extends BaseActivity<T> {
    @OnClick(R.id.image_back)
    public void  clickBackBtn(){
        beforeFinish();
        removeActivity();
    }

    @Override
    protected boolean isChildPage() {
        return true;
    }

}
