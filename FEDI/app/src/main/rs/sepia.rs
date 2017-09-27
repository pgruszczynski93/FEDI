#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

const float3 sepia_red = {0.393f, 0.769f, 0.189f};  // wspolczynnik sepii kanału R
const float3 sepia_green = {0.349f, 0.686, 0.168f}; // wspolczynnik sepii kanału G
const float3 sepia_blue = {0.272f, 0.534f, 0.131f}; // wspolczynnik sepii kanału B

// kernel obliczający wartość piksela w celu uzyskania efektu sepii
uchar4 __attribute__((kernel)) sepia(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 pix_to_float = rsUnpackColor8888(pixel_in);
    float3 out;
    out.r = dot(pix_to_float.rgb, sepia_red);
    out.g = dot(pix_to_float.rgb, sepia_green);
    out.b = dot(pix_to_float.rgb, sepia_blue);
    out = Clamp01Float3(out);
    return rsPackColorTo8888(out);
}

