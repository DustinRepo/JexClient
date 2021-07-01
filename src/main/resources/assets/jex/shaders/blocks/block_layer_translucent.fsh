#version 150 core

#import <sodium:blocks/base.fsh>

uniform float Alpha = 1.1;

void main() {
    vec4 sampleBlockTex = texture(u_BlockTex, v_TexCoord);
    vec4 sampleLightTex = texture(u_LightTex, v_LightCoord);

    vec4 diffuseColor = (sampleBlockTex * sampleLightTex);
    if (diffuseColor.a == 0) {
        discard;
    }
    diffuseColor *= v_Color;
    vec4 final = _linearFog(diffuseColor, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd);
    if (Alpha > 1.0 || Alpha > final.a) {
        fragColor = final;
        return;
    }
    fragColor = vec4(final.rgb, Alpha);
}
