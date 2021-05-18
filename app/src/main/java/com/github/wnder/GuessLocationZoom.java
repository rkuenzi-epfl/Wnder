package com.github.wnder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.text.Layout;
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

    private View startPoint;
    private View endPoint;
    private View layout;
    private int zoomAnimationTime;
    private List<View> toHideWhenZoomedIn;
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
        final Rect startState = new Rect();
        startPoint.getGlobalVisibleRect(startState);
        final Rect endState = new Rect();
        final android.graphics.Point offset = new android.graphics.Point();
        layout.getGlobalVisibleRect(endState, offset);
        startState.offset(-offset.x, -offset.y);
        endState.offset(-offset.x, -offset.y);

        //calculate start scaling factor
        float startScale;
        if((float) endState.width() / endState.height() > (float) startState.width() / startState.height()){
            startScale = (float) startState.height() / endState.height();
        }
        else{
            startScale = (float) startState.width() / endState.width();
        }

        //Start animation from top right corner
        endPoint.setPivotX(ZERO);
        endPoint.setPivotY(ZERO);

        //Choose between zooming in and zooming out
        if(zoomId == ZOOM_IN){
            zoomIn(startState, endState, startScale);
        }
        else{
            zoomOut(startState, endState, startScale);
        }


    }

    /**
     * Zooms in the image card
     * @param startState Rectangle denoting the start emplacement of the card
     * @param endState Rectangle denoting the end emplacement of the card
     * @param startScale Scale of the start card w.r.t the end card
     */
    private void zoomIn(Rect startState, Rect endState, float startScale){
        //Hide the buttons
        for(View view: toHideWhenZoomedIn){
            view.setVisibility(INVISIBLE);
        }

        //Change visibility of the cards
        startPoint.setVisibility(INVISIBLE);
        endPoint.setVisibility(VISIBLE);

        //Animation setup
        AnimatorSet set = new AnimatorSet();

        set.play(ObjectAnimator.ofFloat(endPoint, X, startState.left, endState.left))
                .with(ObjectAnimator.ofFloat(endPoint, Y, startState.top, endState.top))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_X, startScale, ONE))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_Y, startScale, ONE));

        set.setDuration(zoomAnimationTime);
        set.setInterpolator(new DecelerateInterpolator());

        //Animation start
        set.start();
    }

    /**
     * Zooms in the image card
     * @param startState Rectangle denoting the start of the little card
     * @param endState Rectangle denoting the emplacement of the big card
     * @param startScale Scale of the little start card w.r.t the big card
     */
    private void zoomOut(Rect startState, Rect endState, float startScale){
        //Setup animator
        AnimatorSet set = new AnimatorSet();

        set.play(ObjectAnimator.ofFloat(endPoint, X, endState.left, startState.left))
                .with(ObjectAnimator.ofFloat(endPoint, Y, endState.top, startState.top))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_X, ONE, startScale))
                .with(ObjectAnimator.ofFloat(endPoint, SCALE_Y, ONE, startScale));

        set.setDuration(zoomAnimationTime);
        set.setInterpolator(new DecelerateInterpolator());

        //When finished, switch visible cards + show the buttons again
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startPoint.setVisibility(VISIBLE);
                endPoint.setVisibility(GONE);
                for(View view: toHideWhenZoomedIn){
                    view.setVisibility(VISIBLE);
                }
            }
        });

        //Animation start
        set.start();
    }
}
