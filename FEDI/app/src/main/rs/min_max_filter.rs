#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;
uint32_t type;


void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

uchar4 __attribute__((kernel)) min_max_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    uint32_t pos_x = x+1, pos_y = y+1;
    float mask_maxr = 0.0f, mask_maxg = 0.0f, mask_maxb = 0.0f, mask_minr = 1.0f, mask_ming = 1.0f, mask_minb = 1.0f;

    if(pos_x >= 1 && pos_x < width-1 && pos_y >= 1 && pos_y < height-1){
        for(int32_t k=-1; k<=1; k++){
            for(int32_t l=-1; l<=1; l++){
                rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
                if(rgba.r > mask_maxr)
                    mask_maxr = rgba.r;
                if(rgba.g > mask_maxg)
                    mask_maxg = rgba.g;
                if(rgba.b > mask_maxb)
                    mask_maxb = rgba.b;

                if(rgba.r < mask_minr)
                    mask_minr = rgba.r;
                if(rgba.g < mask_ming)
                    mask_ming = rgba.g;
                if(rgba.b < mask_minb)
                    mask_minb = rgba.b;

            }
        }
        if(!type){
            rgba.r = mask_maxr;
            rgba.g = mask_maxg;
            rgba.b = mask_maxb;
        }
        else{
            rgba.r = mask_minr;
            rgba.g = mask_ming;
            rgba.b = mask_minb;
        }
    }
    else{
        rgba = 1.0;
    }

    return rsPackColorTo8888(rgba.rgb);
}