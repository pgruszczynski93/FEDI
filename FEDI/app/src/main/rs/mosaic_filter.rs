#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t size;

uchar4 __attribute__((kernel)) mosaic(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float4 newfull_pix;
    float3 rgb;
    uint32_t mosaic_x, mosaic_y;
    if( !(y%size) && !(x%size) ){
        newfull_pix = full_pix;
    }
    else{
        mosaic_x = size * (x/size);
        mosaic_y = size * (y/size);
        newfull_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, mosaic_x, mosaic_y));
    }
    rgb = newfull_pix.rgb;
    return rsPackColorTo8888(rgb);
}