package com.example.voxriders;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class OurAccessibilityService extends AccessibilityService{
    static final String tripButton="com.ubercab:id/small_nav_grid_tile_item_overlay";
    static final String destTextView="com.ubercab:id/ub__location_edit_search_destination_view";
    static final String destEditText="com.ubercab:id/ub__location_edit_search_destination_edit";
    static final String sourceTextView="com.ubercab:id/ub__location_edit_search_pickup_view";
    static final String sourceEditText="com.ubercab:id/ub__location_edit_search_pickup_edit";
    static final String listOfResults="com.ubercab:id/list";
    static final String requestButton="com.ubercab:id/ub__request_button";
    static final String finalRequestButton="com.ubercab:id/confirm_button_location_editor_sheet_section";
    boolean steps[];
    public void onServiceConnected()
    {
        steps=new boolean[10];
        Log.d("Event","Event occurred: on ServiceConnected");
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences preferences=getSharedPreferences( "data", Context.MODE_PRIVATE);
        final String source=preferences.getString("from","N\\A");
        final String destination=preferences.getString("to","N\\A");
        if(preferences.getBoolean("isIdempotent",false))
        {
            steps[0]=false;
            steps[1]=false;
            steps[2]=false;
            steps[3]=false;
            steps[4]=false;
            steps[5]=false;
            steps[6]=false;
            SharedPreferences.Editor editor=preferences.edit();
            editor.putBoolean("isIdempotent",false);
            editor.commit();
        }
        Log.d("Event","Event occurred: on AccessibilityEvent");
        AccessibilityNodeInfo root=event.getSource(),target;
        if(!steps[0] && (target=getNode(root,tripButton))!=null) {
            target.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            steps[0]=true;
            Log.d("Event", "Step 0 Complete");
        }
        else if(steps[0] && (target=getNode(root,sourceTextView))!=null && !steps[1]) {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            steps[1]=true;
            Log.d("Event", "Step 1 Complete");
        }
        else if(steps[1] && (target=getNode(root,sourceTextView))!=null && !steps[2]) {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            steps[2]=true;
            Log.d("Event", "Step 2 Complete");
        }
        else if(steps[2] && (target=getNode(root,sourceEditText))!=null && !steps[3]) {
            Bundle bundle=new Bundle();
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,source);
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,bundle);
            steps[3]=true;
            Log.d("Event", "Step 3 Complete");
        }
        else if(steps[3] && (target=getNode(root,sourceEditText))!=null && target.getText().length()>0 && (target=getNode(root,listOfResults))!=null
                && !steps[4]) {
            if(target.getChildCount()>1)
            {
                Log.d("CCC",target.getChildCount()+"");
                Log.d("CCC",target.getChild(0).toString());
                if(target.getChild(0).getContentDescription()==null)
                    return;
                target.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                steps[4]=true;
                Log.d("Event", "Step 4 Complete");
            }
        }
        else if(steps[4] && (target=getNode(root,destEditText))!=null && steps[4] && !steps[5]) {
            Bundle bundle=new Bundle();
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,destination);
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,bundle);
            steps[5]=true;
            Log.d("Event", "Step 5 Complete");
        }
        else if(steps[5] && (target=getNode(root,destEditText))!=null && target.getText().length()>0 && (target=getNode(root,listOfResults))!=null
                 && !steps[6]) {
            if(target.getChildCount()>1)
            {
                AccessibilityNodeInfo results=target.getChild(0);
                List<AccessibilityNodeInfo.AccessibilityAction> actionList=results.getActionList();
                if(!"Set this as destination location".equalsIgnoreCase(actionList.get(actionList.size()-1).getLabel().toString()))
                    return;
                target.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                steps[6]=true;
                steps[7]=false;
                Log.d("Event", "Step 6 Complete");
            }
        }
        else if((target=getNode(root,requestButton))!=null && !steps[7])
        {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            steps[7]=true;
            steps[8]=false;
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("Event","Event occurred: onInterrupt");
    }

    private AccessibilityNodeInfo getNode(AccessibilityNodeInfo root,String targetId)
    {
        if(root==null)
            return null;
        List<AccessibilityNodeInfo> listOfNodes=root.findAccessibilityNodeInfosByViewId(targetId);
        if(listOfNodes==null || listOfNodes.isEmpty())
        {
            Log.d("Event","Found");
            return null;
        }
        Log.d("Event","Not Found");
        return listOfNodes.get(0);
    }
}
