package com.airbnb.epoxy.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.airbnb.epoxy.R;
import com.airbnb.epoxy.sample.SampleController.AdapterCallbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example activity usage for {@link com.airbnb.epoxy.EpoxyController}.
 */
public class MainActivity extends AppCompatActivity implements AdapterCallbacks {
  private static final Random RANDOM = new Random();
  private static final int SPAN_COUNT = 6;
  private final SampleController controller = new SampleController(this);
  private List<ColorData> colorDataArrayList = new ArrayList<>();

  private static int randomColor() {
    int r = RANDOM.nextInt(256);
    int g = RANDOM.nextInt(256);
    int b = RANDOM.nextInt(256);

    return Color.rgb(r, g, b);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    controller.setSpanCount(SPAN_COUNT);
    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
    gridLayoutManager.setSpanSizeLookup(controller.getSpanSizeLookup());
    recyclerView.setLayoutManager(gridLayoutManager);

    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(controller.getAdapter());

    colorDataArrayList = constructColorDataList();
    updateController();
  }

  private List<ColorData> constructColorDataList() {

    List<ColorData> result = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      result.add(new ColorData(randomColor(), result.size()));
    }

    return result;
  }

  private void updateController() {
    controller.setData(colorDataArrayList);
  }

  @Override
  public void onColorClicked(ColorData colorData) {
    colorData.setPlayAnimation(!colorData.shouldPlayAnimation());
    updateController();
  }
}
