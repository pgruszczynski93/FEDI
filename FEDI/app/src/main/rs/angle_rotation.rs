#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed



uchar4 __attribute__((kernel)) angle_rotation(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba = rsUnpackColor8888(in);

    return rsPackColorTo8888(rgba.rgb);
}