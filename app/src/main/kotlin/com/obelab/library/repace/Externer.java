package com.obelab.library.repace;


import com.obelab.library.repace.data.LTAnalysis;
import com.obelab.library.repace.data.LTExercise;
import com.obelab.library.repace.data.LTPrescription;
import com.obelab.library.repace.data.LTProtocol;
import com.obelab.library.repace.data.LTTraining;

import java.util.ArrayList;
import java.util.List;

public class Externer {
    static {
        System.loadLibrary("Control");
    }

    public static native LTProtocol getProtocol(
            int type,
            int age,
            int distance,
            int number,
            double time
    );

    public static native LTAnalysis getAnalysis(
            int stage,
            int protocol,
            double lactate,
            double baselineSmo2,
            double currSmo2,
            ArrayList<byte[]> rowData
    );

    public static native int[] getPurpose(
            int protocol,
            double speed
    );

    public static native LTPrescription getPrescription(
            int protocol,
            int purpose,
            double speed
    );

    public static native List<LTTraining> getPrescriptionTable(
            int protocol,
            int purpose,
            double speed
    );

    public static native LTExercise getExercise(
            int age,
            double speed
    );

}