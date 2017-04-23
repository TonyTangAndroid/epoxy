package com.airbnb.epoxy.sample;

import com.airbnb.epoxy.TypedEpoxyController;
import com.airbnb.epoxy.sample.models.ColorModel_;

import java.util.List;

public class SampleController extends TypedEpoxyController<List<ColorData>> {
  private final AdapterCallbacks callbacks;

  SampleController(AdapterCallbacks callbacks) {
    this.callbacks = callbacks;
    setDebugLoggingEnabled(true);
  }

  @Override
  protected void buildModels(List<ColorData> colorDataList) {

    for (ColorData colorData : colorDataList) {
      add(new ColorModel_()
          .id(colorData.getId())
          .color(colorData.getColorInt())
          .playAnimation(colorData.shouldPlayAnimation())
          .clickListener(
              (model, parentView, clickedView, position) -> callbacks.onColorClicked(colorData)));
    }
  }

  @Override
  protected void onExceptionSwallowed(RuntimeException exception) {
    // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
    // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
    throw exception;
  }

  public interface AdapterCallbacks {
    void onColorClicked(ColorData carousel);
  }
}
