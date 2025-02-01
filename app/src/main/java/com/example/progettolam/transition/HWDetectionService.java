//package com.example.progettolam.transition;
//
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import com.huawei.hmf.tasks.OnSuccessListener;
//import com.huawei.hms.location.ActivityConversionInfo;
//import com.huawei.hms.location.ActivityConversionRequest;
//import com.huawei.hms.location.ActivityIdentification;
//import com.huawei.hms.location.ActivityIdentificationData;
//import com.huawei.hms.location.ActivityIdentificationService;
//import com.huawei.hmf.tasks.Task;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.PriorityQueue;
//
//public class HWDetectionService {
//
//        private ActivityIdentificationService activityService;
//        private PendingIntent pendingIntent;
//        private Context context;
//
//        public HWDetectionService(Context context) {
//            this.context = context;
//            this.activityService = ActivityIdentification.getService(context);
//        }
//        public ActivityConversionRequest buildTransitionRequest(){
//            List<ActivityConversionInfo> list = new ArrayList<>();
//            // 创建一个静止状态进入活动转换信息对象
//            list.add(new ActivityConversionInfo(ActivityIdentificationData.STILL,
//                    ActivityConversionInfo.ENTER_ACTIVITY_CONVERSION));
//            // 创建一个静止状态退出活动转换信息对象
//            list.add( new ActivityConversionInfo(ActivityIdentificationData.STILL,
//                    ActivityConversionInfo.EXIT_ACTIVITY_CONVERSION));
//
//            list.add(new ActivityConversionInfo(ActivityIdentificationData.WALKING,
//                    ActivityConversionInfo.ENTER_ACTIVITY_CONVERSION));
//            // 创建一个静止状态退出活动转换信息对象
//            list.add( new ActivityConversionInfo(ActivityIdentificationData.WALKING,
//                    ActivityConversionInfo.EXIT_ACTIVITY_CONVERSION));
//
//            list.add(new ActivityConversionInfo(ActivityIdentificationData.VEHICLE,
//                    ActivityConversionInfo.ENTER_ACTIVITY_CONVERSION));
//            // 创建一个静止状态退出活动转换信息对象
//            list.add( new ActivityConversionInfo(ActivityIdentificationData.VEHICLE,
//                    ActivityConversionInfo.EXIT_ACTIVITY_CONVERSION));
//
//            list.add(new ActivityConversionInfo(ActivityIdentificationData.OTHERS,
//                    ActivityConversionInfo.ENTER_ACTIVITY_CONVERSION));
//            // 创建一个静止状态退出活动转换信息对象
//            list.add( new ActivityConversionInfo(ActivityIdentificationData.OTHERS,
//                    ActivityConversionInfo.EXIT_ACTIVITY_CONVERSION));
////            ActivityConversionRequest request = new ActivityConversionRequest();
////            request.setActivityConversions(list);
//            return new ActivityConversionRequest(list);
//        }
//        public void startActivityRecognition(ActivityConversionRequest request, PendingIntent pendingIntent) {
//            activityService.createActivityConversionUpdates(request, pendingIntent)
//                .addOnSuccessListener(aVoid -> Log.d("HMS", "Activity recognition started"))
//                .addOnFailureListener(e -> Log.e("HMS", "Error: " + e.getMessage()));
//        }
//
//        public void stopActivityRecognition( PendingIntent pendingIntent) {
//            activityService.deleteActivityIdentificationUpdates(pendingIntent)
//                    .addOnSuccessListener(aVoid -> Log.d("HMS", "Activity recognition stopped"))
//                    .addOnFailureListener(e -> Log.e("HMS", "Error: " + e.getMessage()));
//        }
//
//}
//
