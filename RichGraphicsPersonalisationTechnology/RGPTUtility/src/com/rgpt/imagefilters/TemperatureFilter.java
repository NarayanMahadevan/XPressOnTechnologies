package com.rgpt.imagefilters;

import java.awt.image.BufferedImage;

public class TemperatureFilter extends PointFilter
{
  private float temperature = 6650.0F;
  private float rFactor;
  private float gFactor;
  private float bFactor;
  static float[] blackBodyRGB = { 1.0F, 0.0337F, 0.0F, 1.0F, 0.0592F, 0.0F, 1.0F, 0.0846F, 0.0F, 1.0F, 0.1096F, 0.0F, 1.0F, 0.1341F, 0.0F, 1.0F, 0.1578F, 0.0F, 1.0F, 0.1806F, 0.0F, 1.0F, 0.2025F, 0.0F, 1.0F, 0.2235F, 0.0F, 1.0F, 0.2434F, 0.0F, 1.0F, 0.2647F, 0.0033F, 1.0F, 0.2889F, 0.012F, 1.0F, 0.3126F, 0.0219F, 1.0F, 0.336F, 0.0331F, 1.0F, 0.3589F, 0.0454F, 1.0F, 0.3814F, 0.0588F, 1.0F, 0.4034F, 0.0734F, 1.0F, 0.425F, 0.0889F, 1.0F, 0.4461F, 0.1054F, 1.0F, 0.4668F, 0.1229F, 1.0F, 0.487F, 0.1411F, 1.0F, 0.5067F, 0.1602F, 1.0F, 0.5259F, 0.18F, 1.0F, 0.5447F, 0.2005F, 1.0F, 0.563F, 0.2216F, 1.0F, 0.5809F, 0.2433F, 1.0F, 0.5983F, 0.2655F, 1.0F, 0.6153F, 0.2881F, 1.0F, 0.6318F, 0.3112F, 1.0F, 0.648F, 0.3346F, 1.0F, 0.6636F, 0.3583F, 1.0F, 0.6789F, 0.3823F, 1.0F, 0.6938F, 0.4066F, 1.0F, 0.7083F, 0.431F, 1.0F, 0.7223F, 0.4556F, 1.0F, 0.736F, 0.4803F, 1.0F, 0.7494F, 0.5051F, 1.0F, 0.7623F, 0.5299F, 1.0F, 0.775F, 0.5548F, 1.0F, 0.7872F, 0.5797F, 1.0F, 0.7992F, 0.6045F, 1.0F, 0.8108F, 0.6293F, 1.0F, 0.8221F, 0.6541F, 1.0F, 0.833F, 0.6787F, 1.0F, 0.8437F, 0.7032F, 1.0F, 0.8541F, 0.7277F, 1.0F, 0.8642F, 0.7519F, 1.0F, 0.874F, 0.776F, 1.0F, 0.8836F, 0.8F, 1.0F, 0.8929F, 0.8238F, 1.0F, 0.9019F, 0.8473F, 1.0F, 0.9107F, 0.8707F, 1.0F, 0.9193F, 0.8939F, 1.0F, 0.9276F, 0.9168F, 1.0F, 0.9357F, 0.9396F, 1.0F, 0.9436F, 0.9621F, 1.0F, 0.9513F, 0.9844F, 0.9937F, 0.9526F, 1.0F, 0.9726F, 0.9395F, 1.0F, 0.9526F, 0.927F, 1.0F, 0.9337F, 0.915F, 1.0F, 0.9157F, 0.9035F, 1.0F, 0.8986F, 0.8925F, 1.0F, 0.8823F, 0.8819F, 1.0F, 0.8668F, 0.8718F, 1.0F, 0.852F, 0.8621F, 1.0F, 0.8379F, 0.8527F, 1.0F, 0.8244F, 0.8437F, 1.0F, 0.8115F, 0.8351F, 1.0F, 0.7992F, 0.8268F, 1.0F, 0.7874F, 0.8187F, 1.0F, 0.7761F, 0.811F, 1.0F, 0.7652F, 0.8035F, 1.0F, 0.7548F, 0.7963F, 1.0F, 0.7449F, 0.7894F, 1.0F, 0.7353F, 0.7827F, 1.0F, 0.726F, 0.7762F, 1.0F, 0.7172F, 0.7699F, 1.0F, 0.7086F, 0.7638F, 1.0F, 0.7004F, 0.7579F, 1.0F, 0.6925F, 0.7522F, 1.0F, 0.6848F, 0.7467F, 1.0F, 0.6774F, 0.7414F, 1.0F, 0.6703F, 0.7362F, 1.0F, 0.6635F, 0.7311F, 1.0F, 0.6568F, 0.7263F, 1.0F, 0.6504F, 0.7215F, 1.0F, 0.6442F, 0.7169F, 1.0F, 0.6382F, 0.7124F, 1.0F, 0.6324F, 0.7081F, 1.0F, 0.6268F, 0.7039F, 1.0F };

  public TemperatureFilter()
  {
    this.canFilterIndexColorModel = true;
  }

  public void setTemperature(float temperature) {
    this.temperature = temperature;
  }

  public float getTemperature() {
    return this.temperature;
  }

  public BufferedImage filter(BufferedImage src, BufferedImage dst) {
    this.temperature = Math.max(1000.0F, Math.min(10000.0F, this.temperature));

    int t = 3 * (int)((this.temperature - 1000.0F) / 100.0F);
    this.rFactor = (1.0F / blackBodyRGB[t]);
    this.gFactor = (1.0F / blackBodyRGB[(t + 1)]);
    this.bFactor = (1.0F / blackBodyRGB[(t + 2)]);

    float m = Math.max(Math.max(this.rFactor, this.gFactor), this.bFactor);
    this.rFactor /= m;
    this.gFactor /= m;
    this.bFactor /= m;

    return super.filter(src, dst);
  }

  public void setTemperatureFromRGB(int rgb)
  {
    float r = rgb >> 16 & 0xFF;
    float g = rgb >> 8 & 0xFF;
    float b = rgb & 0xFF;

    float rb = r / b;

    int start = 0;
    int end = blackBodyRGB.length / 3;
    int m = (start + end) / 2;

    start = 0; r = blackBodyRGB.length; for (m = (start + end) / 2; end - start > 1; m = (start + end) / 2) {
      int m3 = m * 3;
      if (blackBodyRGB[m3] / blackBodyRGB[(m3 + 2)] > rb)
        start = m;
      else {
        end = m;
      }
    }
    setTemperature(m * 100.0F + 1000.0F);
  }

  public int filterRGB(int x, int y, int rgb) {
    int a = rgb & 0xFF000000;
    int r = rgb >> 16 & 0xFF;
    int g = rgb >> 8 & 0xFF;
    int b = rgb & 0xFF;

    r = (int)(r * this.rFactor);
    g = (int)(g * this.gFactor);
    b = (int)(b * this.bFactor);

    return a | r << 16 | g << 8 | b;
  }

  public int[] getLUT() {
    int[] lut = new int[256];
    for (int i = 0; i < 256; i++) {
      lut[i] = filterRGB(0, 0, i << 24 | i << 16 | i << 8 | i);
    }
    return lut;
  }

  public String toString() {
    return "Colors/Temperature...";
  }
}