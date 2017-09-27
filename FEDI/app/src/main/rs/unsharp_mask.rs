#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation blurred_img, org_img, unsharp_img; // alokacje: zdjęcie rozmyte, oryginalne, maska wyostrzająca
float threshold;    // próg


// kernel odpowiedzialny za wygenerowanie piksela będącego roznicą piksela z obrazu oryginalnego i rozmytego
uchar4 __attribute__((kernel)) unsharp_mask(uchar4 in, uint32_t x, uint32_t y) {
    float4 org_rgba = rsUnpackColor8888(in), blurred_rgba;
    float3 org_rgb = org_rgba.rgb;
    blurred_rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(blurred_img, x, y));
    org_rgb -= blurred_rgba.rgb;
    org_rgb = Clamp01Float3(org_rgb);
    return rsPackColorTo8888(org_rgb);
}

// kernel odpowiedzialny za wyostrzanie piksela metoda unsharp mask
uchar4 __attribute__((kernel)) unsharp_mask_mix(uchar4 in, uint32_t x, uint32_t y) {
    float4 contrast_rgba = rsUnpackColor8888(in);
    float3 contrast_rgb = contrast_rgba.rgb;
    float4 unsharp_rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(unsharp_img, x, y));
    float3 unsharp_rgb = unsharp_rgba.rgb;
    float4 org_rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(org_img, x, y));
    float3 org_rgb = org_rgba.rgb;

    float3 out;
    float blured_luminance;
    float org_luminance, diff;


    blured_luminance = GetY(unsharp_rgb);// obliczenie luminancji piksela obrazu rozmytego
    org_luminance = GetY(org_rgb); // obliczenie luminancji piksela obrazu oryinalnego
    diff = blured_luminance - org_luminance;    // obliczenie roznicy luminancji
    if(fabs(diff) < threshold){
    //wyostrzanie
        out = org_rgb + unsharp_rgb * contrast_rgb*1.33;
    }
    //if(luminance_percent < threshold){
    //    out = org_rgb + unsharp_rgb * contrast_rgb;
    //}
    else{
    //przepisanie piksela oryginalnego
        out = org_rgb;
    }
    return rsPackColorTo8888(Clamp01Float3(out));
}
