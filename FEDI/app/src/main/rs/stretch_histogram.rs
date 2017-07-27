#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_script render_script;
rs_allocation img_in, img_out;
int32_t width, height;
float min_r = 1.0f, min_g = 1.0f, min_b = 1.0f;
float max_r = 0.0f, max_g = 0.0f, max_b = 0.0f;

static void setup(){
    float4 curr_pix;
    float3 curr_rgb;
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);

    for(int32_t  i=0; i<width; i++){
        for(int32_t j=0; j<height; j++){
            curr_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, i, j));
            curr_rgb = curr_pix.rgb;
            if (curr_rgb.r < min_r) min_r = curr_rgb.r;
            if (curr_rgb.g < min_g) min_g = curr_rgb.g;
            if (curr_rgb.b < min_b) min_b = curr_rgb.b;

            if (curr_rgb.r > max_r) max_r = curr_rgb.r;
            if (curr_rgb.g > max_g) max_g = curr_rgb.g;
            if (curr_rgb.b > max_b) max_b = curr_rgb.b;
        }
    }
}

void root(const uchar4 *pixel_in, uchar4 *pixel_out, const void *usrData, uint32_t x, uint32_t y) {
    float4 full_pix = rsUnpackColor8888(*pixel_in);
    float3 rgb = full_pix.rgb;

    if(((min_r != 1.0) && (min_g != 1.0) && (min_b != 1.0)) && ((max_r != 0.0) && (max_g != 0.0) && (max_b != 0.0))) {
        rgb.r = (1.0f * (rgb.r - min_r))/(max_r - min_r);
        rgb.g = (1.0f * (rgb.g - min_g))/(max_g - min_g);
        rgb.b = (1.0f * (rgb.b - min_b))/(max_b - min_b);
    }
    else
        rgb = full_pix.rgb;

    *pixel_out = rsPackColorTo8888(rgb);
}

void filter(){
    setup();
    rsForEach(render_script, img_in, img_out);
}
