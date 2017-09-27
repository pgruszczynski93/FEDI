#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float treshold_value;   // wartość progu

// kernel odpowiedzialny za operacje progowania obrazu
uchar4 __attribute__((kernel)) treshold(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 unpacked_pixel = rsUnpackColor8888(pixel_in);
    float grayscale_value = dot(unpacked_pixel.rgb, yuv_vector);
    grayscale_value = (grayscale_value > treshold_value) ? 1 : 0;
    float3 out = {grayscale_value,grayscale_value,grayscale_value};
    return rsPackColorTo8888(out);
}
