#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

// kernel obliczający skalę szarości piksela na podstawie modelu yuv; luminancji
uchar4 __attribute__((kernel)) grayscale_yuv(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 unpacked_pixel = rsUnpackColor8888(pixel_in);
    float3 multiplied_pixel = dot(unpacked_pixel.rgb, yuv_vector);
    return rsPackColorTo8888(multiplied_pixel);
}