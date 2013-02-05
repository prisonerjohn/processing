/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2011-13 Ben Fry and Casey Reas

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License version 2.1 as published by the Free Software Foundation.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

uniform mat4 modelview;
uniform mat4 transform;
uniform mat3 normalMatrix;
uniform mat4 texcoordMatrix;

uniform int lightCount;
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform vec3 lightAmbient[8];
uniform vec3 lightDiffuse[8];
uniform vec3 lightSpecular[8];      
uniform vec3 lightFalloffCoefficients[8];
uniform vec2 lightSpotParameters[8];

attribute vec4 inVertex;
attribute vec4 inColor;
attribute vec3 inNormal;
attribute vec2 inTexcoord;

attribute vec4 inAmbient;
attribute vec4 inSpecular;
attribute vec4 inEmissive;
attribute float inShine;

varying vec4 vertColor;
varying vec4 vertTexcoord;

const float zero_float = 0.0;
const float one_float = 1.0;
const vec3 zero_vec3 = vec3(0);

float falloffFactor(vec3 lightPos, vec3 vertPos, vec3 coeff) {
  vec3 lpv = lightPos - vertPos;
  vec3 dist = vec3(one_float);
  dist.z = dot(lpv, lpv);
  dist.y = sqrt(dist.z);
  return one_float / dot(dist, coeff);
}

float spotFactor(vec3 lightPos, vec3 vertPos, vec3 lightNorm, float minCos, float spotExp) {
  vec3 lpv = normalize(lightPos - vertPos);
  vec3 nln = -one_float * lightNorm;
  float spotCos = dot(nln, lpv);
  return spotCos <= minCos ? zero_float : pow(spotCos, spotExp);
}

float lambertFactor(vec3 lightDir, vec3 vecNormal) {
  return max(zero_float, dot(lightDir, vecNormal));
}

float blinnPhongFactor(vec3 lightDir, vec3 vertPos, vec3 vecNormal, float shine) {
  vec3 np = normalize(vertPos);
  vec3 ldp = normalize(lightDir - np);
  return pow(max(zero_float, dot(ldp, vecNormal)), shine);
}

void main() {
  // Vertex in clip coordinates
  gl_Position = transform * inVertex;
    
  // Vertex in eye coordinates
  vec3 ecVertex = vec3(modelview * inVertex);
  
  // Normal vector in eye coordinates
  vec3 ecNormal = normalize(normalMatrix * inNormal);
  
  if (dot(-one_float * ecVertex, ecNormal) < zero_float) {
    // If normal is away from camera, choose its opposite.
    // If we add backface culling, this will be backfacing  
    ecNormal *= -one_float;
  }
  
  // Light calculations
  vec3 totalAmbient = vec3(0, 0, 0);
  vec3 totalDiffuse = vec3(0, 0, 0);
  vec3 totalSpecular = vec3(0, 0, 0);
  for (int i = 0; i < 8; i++) {
    if (lightCount == i) break;
    
    vec3 lightPos = lightPosition[i].xyz;
    bool isDir = zero_float < lightPosition[i].w;
    float spotCos = lightSpotParameters[i].x;
    float spotExp = lightSpotParameters[i].y;
    
    vec3 lightDir;
    float falloff;    
    float spotf;
      
    if (isDir) {
      falloff = one_float;
      lightDir = -one_float * lightNormal[i];
    } else {
      falloff = falloffFactor(lightPos, ecVertex, lightFalloffCoefficients[i]);  
      lightDir = normalize(lightPos - ecVertex);
    }
  
    spotf = spotExp > zero_float ? spotFactor(lightPos, ecVertex, lightNormal[i], 
                                              spotCos, spotExp) 
                                 : one_float;
    
    if (any(greaterThan(lightAmbient[i], zero_vec3))) {
      totalAmbient  += lightAmbient[i] * falloff;
    }
    
    if (any(greaterThan(lightDiffuse[i], zero_vec3))) {
      totalDiffuse  += lightDiffuse[i] * falloff * spotf * 
                       lambertFactor(lightDir, ecNormal);
    }
    
    if (any(greaterThan(lightSpecular[i], zero_vec3))) {
      totalSpecular += lightSpecular[i] * falloff * spotf * 
                       blinnPhongFactor(lightDir, ecVertex, ecNormal, inShine);
    }   
  }    
  
  // Calculating final color as result of all lights (plus emissive term).
  // Transparency is determined exclusively by the diffuse component.
  vertColor = vec4(totalAmbient, 0) * inAmbient + 
              vec4(totalDiffuse, 1) * inColor + 
              vec4(totalSpecular, 0) * inSpecular + 
              vec4(inEmissive.rgb, 0); 
              
  // Calculating texture coordinates, with r and q set both to one
  vertTexcoord = texcoordMatrix * vec4(inTexcoord, 1.0, 1.0);        
}
