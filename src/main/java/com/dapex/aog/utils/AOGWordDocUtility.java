package com.dapex.aog.utils;

import com.dapex.aog.config.Constants;

import static com.dapex.aog.config.Constants.*;

/**
 * Created by taddai on 2/8/19.
 */
public class AOGWordDocUtility {

    public static String getAOGWordDoc(String label) {
        switch (label) {
            case AOG_KA_LABEL:
               return AOG_KA;
            case AOG_NA_LABEL:
               return AOG_NA;
            default:
               return AOG_NA;
        }
    }

    public static String getAOP(Integer platformId) {
        String aop = "";
        if (platformId == UNET_NA_ID)
            aop = UNET_NA_NAME;
        if (platformId == UNET_KA_ID)
            aop = UNET_KA_NAME;
        if (platformId == SPECIALTY_BENEFITS_ID)
            aop = SPECIALTY_BENEFITS;
        if (platformId == NICE_ID)
            aop = NICE;
        if (platformId == OXFORD_ID)
            aop = OXFORD;
        if (platformId == USP_OXFORD_ID)
            aop = USP_OXFORD;
        if (platformId == MR_ARCHIVE_ID)
            aop = MR_ARCHIVE;
        if (platformId == MR_ID)
            aop = MR;
        if (platformId == CIRRUS_ID)
            aop = CIRRUS;
        if (platformId == HIDDEN_ID)
            aop = HIDDEN;
        if (platformId == UNET_NA_ID)
            aop = UNET_NA;
        if (platformId == UNET_KA_ID)
            aop = UNET_KA;
        if (platformId == SUREST_ID)
            aop = Surest;
        if (platformId == USP_KA_ID)
            aop = USP_KA;
        if (platformId == USP_NA_ID)
            aop = USP_NA;
        if (platformId == USP_WEST_ID)
            aop = USP_West;
        return aop;

    }
































    public static String getOOSBucketName(String label) {
        switch (label) {
            case AOG_KA_LABEL:
                return BUCKET_NAME_KA;
            case AOG_NA_LABEL:
                return BUCKET_NAME_NA;
            default:
                return BUCKET_NAME;
        }
    }
}
