#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;   // alokacja wejściowa
uint32_t size;          // rozmiar kafelka tworzącego mozaikę

// kernel odpowiedzialny za stworzoenei efektu ufo mozaiki
uchar4 __attribute__((kernel)) ufo_mosaic(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float4 newfull_pix;
    float3 rgb;
    uint32_t mosaic_x, mosaic_y;

    mosaic_x = (sqrt((float)(x*x + y*y))/32)*32;
    mosaic_y = ((atan((float)(y/x))*180/3.14)/32)*32;
    //mosaic_x = (sqrt((float)(x*x + y*y))/size)*size;
//    mosaic_y = (atan((float)(y/x))/size)*size;
    newfull_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, mosaic_x, mosaic_y));
    rgb = newfull_pix.rgb;
    return rsPackColorTo8888(rgb);
}