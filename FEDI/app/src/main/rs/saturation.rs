#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

// wartość nasycenia
float saturation_value;

// kernel modyfikujący wartość nasycenia piksela
uchar4 __attribute__((kernel)) saturation(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 saturated_pixel = dot(full_pixel.rgb, yuv_vector);   //iloczyn skalarny wartości piksela i wartości yuv
    saturated_pixel = mix(saturated_pixel, full_pixel.rgb, saturation_value); //miesza 2 wartości według wzoru saturated_pixel + ((full_pixel.rgb - saturated_pixel) * saturation_value).
    return rsPackColorTo8888(saturated_pixel);
}
