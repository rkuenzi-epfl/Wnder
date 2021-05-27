package com.github.wnder.guessLocation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;
import static android.view.View.VISIBLE;
import static android.view.View.X;
import static android.view.View.Y;

/**
 * Zoom animation principally conceived for zooming in the image little overview in guess location activity
 */
public class GuessLocationZoom {

    private final View startPoint;
    private final View endPoint;
    private final View layout;
    private final int zoomAnimationTime;
    private final List<View> toHideWhenZoomedIn;
    private final static float ZERO = 0f;
    private final static float ONE = 1f;
    private final static String ZOOM_IN = "zoom_in";
    private final static String ZOOM_OUT = "zoom_out";

    /**
     * Constructor for zoom animation
     * @param startPoint little view to zoom in
     * @param endPoint big view to zoom out
     * @param layout global layout
     * @param zoomAnimationTime animation time (in ms)
     * @param toHideWhenZoomedIn a list of views to hide once zoomed in, can be empty
     */
    public GuessLocationZoom(View startPoint, View endPoint, View layout, int zoomAnimationTime, List<View> toHideWhenZoomedIn){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.layout = layout;
        this.zoomAnimationTime = zoomAnimationTime;
        this.toHideWhenZoomedIn = toHideWhenZoomedIn;

        startPoint.setOnClickListener(id -> zoom(ZOOM_IN));
        endPoint.setOnClickListener(id -> zoom(ZOOM_OUT));
    }

    /**
     * Zooms in or out of a card depending on the argument
     * @param zoomId "zoom_in" to zoom in, "zoom_out" otherwise
     */
    private void zoom(String zoomId){
        //setup beginning and end forms of the card for the animation.
        final Rect littlePic = new Rect();
        startPoint.getGlobalVisibleRect(littlePic);
        final Rect bigPic = new Rect();
        final android.graphics.Point offset = new android.graphics.Point();
        layout.getGlobalVisibleRect(bigPic, offset);
        littlePic.offset(-offset.x, -offset.y);
        bigPic.offset(-offset.x, -offset.y);

        //calculate start scaling factor
        float littlePicScale;
        if((float) bigPic.width() / bigPic.height() > (float) littlePic.width() / littlePic.height()){
            littlePicScale = (float) littlePic.height() / bigPic.height();
        }
        else{
            littlePicScale = (float) littlePic.width() / bigPic.width();
        }

        //Start animation from top right corner
        endPoint.setPivotX(ZERO);
        endPoint.setPivotY(ZERO);

        //Choose between zooming in and zooming out
        if(zoomId.equals(ZOOM_IN)){
            zoomIn(littlePic, bigPic, littlePicScale);
        }
        else{
            zoomOut(littlePic, bigPic, littlePicScale);
        }
    }

    /**
     * Zooms in the image card
     * @param littlePic Rectangle denoting the start emplacement of the card
     * @param bigPic Rectangle denoting the end emplacement of the card
     * @param littlePicScale Scale of the start card w.r.t the end card
     */
    private void zoomIn(Rect littlePic, Rect bigPic, float littlePicScale){
        //Hide the buttons
        for(View view: toHideWhenZoomedIn){
            view.setVisibility(INVISIBLE);
        }

        //Change visibility of the cards
        startPoint.setVisibility(INVISIBLE);
        endPoint.setVisibility(VISIBLE);

        //Animation setup
        AnimatorSet set = defineAnimation(littlePic, bigPic, littlePicScale, ONE);

        //Animation start
        set.start();
    }

    /**
     * Zooms in the image card
     * @param littlePic Rectangle denoting the start of the little card
     * @param bigPic Rectangle denoting the emplacement of the big card
     * @param littlePicScale Scale of the little start card w.r.t the big card
     */
    private void zoomOut(Rect littlePic, Rect bigPic, float littlePicScale){
        //Setup animator
        AnimatorSet set = defineAnimation(bigPic, littlePic, ONE, littlePicScale);

        //When finished, switch visible cards + show the buttons again
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startPoint.setVisibility(VISIBLE);
                endPoint.setVisibility(GONE);
                //Show the buttons
                for(View view: toHideWhenZoomedIn){
                    view.setVisibility(VISIBLE);
                }
            }
        });

        //Animation start
        set.start();
    }

    /**
     * Defines zoom animation
     * @param startState small or big part
     * @param endState small or big part
     * @param startScale scale of the current shown pic
     * @param endScale scale of the new shown pic (after zoom)
     */
    private AnimatorSet defineAnimation(Rect startState, Rect endState, float startScale, float endScale){
        //Setup animator
        AnimatorSet set = new AnimatorSet();

        set.play(ObjectAnimator.ofFloat(endPoint, X, startState.left, endState.left))
                .with(ObjectAnimator.ofFloat(endPoint, Y, startState.top, endState.top))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_X, startScale, endScale))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_Y, startScale, endScale));

        set.setDuration(zoomAnimationTime);
        set.setInterpolator(new DecelerateInterpolator());

        return set;
    }
}
