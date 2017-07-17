#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

int levels;
//oprawic ten filtr
uchar4 __attribute__((kernel)) grayscale_xlevels(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float gray = dot(rgb.rgb, yuv_vector);
    float conversion_factor = 1.0 / ((levels + 1));
    float gray_factor = ((gray/conversion_factor)+0.5) * conversion_factor;
    rgb.r = rgb.g = rgb.b = gray_factor;
    return rsPackColorTo8888(rgb);
}
