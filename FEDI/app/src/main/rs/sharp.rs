#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


rs_allocation img_in;
uint32_t width, height;
int intensity;
//static float laplacian[9] = { 0, -1, 0, -1, 0, -1, 0, -1, 0 };

static float laplacian_3[9] = { -1, -1, -1, -1, 9, -1, -1, -1, -1};
static float laplacian_2[9] = { 0, -1, 0, -1, 5, -1, 0, -1, 0};
static float laplacian_1[9] = { 1, -2, 1, -2, 5, -2, 1, -2, 1};
//static float laplacian_0[9] = { 0, -1, 0, -1, 20,  -1, 0, -1, 0};

/*
void init(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
    laplacian[4] = intensity + 8.0f;
}
*/

uchar4 __attribute__((kernel)) sharp(uchar4 pixel_in, uint32_t x, uint32_t y){
 //laplacian[4] = intensity + 5.0f;
 float* laplacian;
 if(!intensity){
    laplacian = laplacian_1;
 }
 else if(intensity==1){
    laplacian = laplacian_2;
 }
 else if(intensity==2){
    laplacian = laplacian_3;
 }
 //else if(intensity==3){
   // laplacian = laplacian_3;
 //}
 float3 new_rgb = {0,0,0};
 uint32_t pos_x = x+1, pos_y = y+1;
 if(pos_x >= 1 && pos_x < width-1 && pos_y >= 1 && pos_y < height-1){
    int32_t kernel_index = 0;
    for(int32_t k=-1; k<=1; k++){
        for(int32_t l=-1; l<=1; l++){
            float4 rgb = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
            new_rgb += rgb.rgb * (*(laplacian+kernel_index));
            ++kernel_index;
        }
    }
    new_rgb = Clamp01Float3(new_rgb);
 }
 else{
    new_rgb = 1.0;
 }
 return rsPackColorTo8888(new_rgb);
}