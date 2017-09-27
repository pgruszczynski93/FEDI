#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;   // alokacja wejściowa
int32_t width, height, mask_type;   // szerokość, wysokość, typ maski
// zdefiniowanie masek przekształceń
static float mask_0[9] = {-1,0,1, -1,0,1, -1,0,1};
static float mask_1[9] = { -2, -2, -0, -2, 6, 0, 0, 0, 0};
static float mask_2[9] = { -4, -4, -0, -4, 12, 0, 0, 0, 0};
static float mask_3[9] = { 0, 0, 0, 0, 6, -2, 0, -2, -2};
static float mask_4[9] = { 0, 0, 0, 0, 12, -4, 0, -4, -4};
static float mask_5[9] = { -1, 0, 1, -1, 1, 1, -1, 0, 1};
static float mask_6[9] = { -1, -1, 0, -1, 1, 1, 0, 1, 1};
static float mask_7[9] = { -1, -1, -1, 0, 1, 0, 1, 1, 1};
static float mask_8[9] = { 0, -1, -1, 1, 1, -1, 1, 1, 0};
static float mask_9[9] = { 1, 0, -1, 1, 1, -1, 1, 0, -1};
static float mask_10[9] = { 1, 1, 0, 1, 1, -1, 0, -1, -1};
static float mask_11[9] = { 1, 1, 1, 0, 1, 0, -1, -1, -1};
static float mask_12[9] = { 0, 1, 1, -1, 1, 1, -1, -1, 0};

// inicjalizacja
void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

// wejscie do odcieni szarosci dla mask <4
// kernel uwypuklający piksel
uchar4 __attribute__((kernel)) emboss_relief_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    float3 new_rgb = {0,0,0};
    uint32_t pos_x = x+1, pos_y = y+1;
    float* mask;

    if(!mask_type){
        mask = mask_0;
    }
    else if(mask_type==1){
        mask = mask_1;
    }
    else if(mask_type==2){
        mask = mask_2;
    }
    else if(mask_type==3){
        mask = mask_3;
    }
    else if(mask_type==4){
        mask = mask_4;
    }
    else if(mask_type==5){
        mask = mask_5;
    }
    else if(mask_type==6){
        mask = mask_6;
    }
    else if(mask_type==7){
        mask = mask_7;
    }
    else if(mask_type==8){
        mask = mask_8;
    }
    else if(mask_type==9){
        mask = mask_9;
    }
    else if(mask_type==10){
        mask = mask_10;
    }
    else if(mask_type==11){
        mask = mask_11;
    }
    else if(mask_type==12){
        mask = mask_12;
    }
    if(pos_x >= 1 && pos_x < width-1 && pos_y >= 1 && pos_y < height-1){
        int32_t kernel_index = 0;
        for(int32_t k=-1; k<=1; k++){
            for(int32_t l=-1; l<=1; l++){
                rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
                if(mask_type <= 4){
                    rgba.rgb = GrayScaleValue01(rgba.rgb);
                }
                new_rgb += rgba.rgb * (*(mask+kernel_index));
                ++kernel_index;
            }
        }
        if(mask_type<=4){
            new_rgb += 0.5f;
        }
        new_rgb = Clamp01Float3(new_rgb);
    }
    else{
        new_rgb = 1.0;
    }
    return rsPackColorTo8888(new_rgb);
}
