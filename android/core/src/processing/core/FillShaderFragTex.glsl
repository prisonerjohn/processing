/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2011 Andres Colubri

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

precision mediump float;

uniform sampler2D textureSampler;

varying vec4 vertColor;
varying vec2 vertTexcoord;

void main() {
  // This implements GL_MODULATE mode. For GL_REPLACE, we just ignore
  // vertColor (or set to 1 in the vertex shader...)
  gl_FragColor = texture2D(textureSampler, vertTexcoord) * vertColor;
}