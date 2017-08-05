#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


rs_allocation img_in;
int32_t width, height;
float threshold;
float level;

void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
    level = 0.2;
}


uchar4 __attribute__((kernel)) salt_pepper_noise(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba = rsUnpackColor8888(in);
    float3 rgb = rgba.rgb;
    float propability = rsRand(0.0f, 1.0f);
    if(propability <= threshold){
        rgb = (!rsRand(0,2)) ? 0.0f : 1.0f;
    }
    else{
        rgb = rgb;
    }
    return rsPackColorTo8888(rgb);
}


uchar4 __attribute__((kernel)) homogeneous_noise(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba = rsUnpackColor8888(in);
    float3 rgb = rgba.rgb;
    float propability = rsRand(0.0f, 1.0f);
    float value = rsRand(level) - level;
    if(propability <= threshold){
        rgb+=value;
        rgb = Clamp01Float3(rgb);
    }
    else{
        rgb = rgb;
    }
    return rsPackColorTo8888(rgb);
}