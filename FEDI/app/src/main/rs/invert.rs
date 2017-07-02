#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

uchar4 __attribute__((kernel)) invert(uchar4 pixel_in, int32_t x, int32_t y){
    uchar4 pixel_out = pixel_in;
    pixel_out.r = 255 - pixel_in.r;
    pixel_out.g = 255 - pixel_in.g;
    pixel_out.b = 255 - pixel_in.b;
    return pixel_out;
}