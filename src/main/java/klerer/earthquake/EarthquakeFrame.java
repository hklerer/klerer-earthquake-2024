package klerer.earthquake;

import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import klerer.earthquake.json.Feature;
import klerer.earthquake.json.FeatureCollection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Arrays;

public class EarthquakeFrame extends JFrame {

    private JList<String> jlist = new JList<>();
    private ButtonGroup buttonGroup = new ButtonGroup();

    public EarthquakeFrame() {

        setTitle("Earthquake Frame");
        setSize(300, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JRadioButton oneHourButton = new JRadioButton("One Hour");
        JRadioButton thirtyDaysButton = new JRadioButton("Thirty Days");

        panel.add(oneHourButton);
        panel.add(thirtyDaysButton);

        buttonGroup.add(oneHourButton);
        buttonGroup.add(thirtyDaysButton);

        add(panel, BorderLayout.NORTH);

        add(jlist, BorderLayout.CENTER);

        EarthquakeService service = new EarthquakeServiceFactory().getService();

        oneHourButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Disposable disposable = service.oneHour()
                        // tells Rx to request the data on a background Thread
                        .subscribeOn(Schedulers.io())
                        // tells Rx to handle the response on Swing's main Thread
                        .observeOn(SwingSchedulers.edt())
                        //.observeOn(AndroidSchedulers.mainThread()) // Instead use this on Android only
                        .subscribe(
                                (response) -> handleResponse(response),
                                Throwable::printStackTrace);
            }
        });

        thirtyDaysButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Disposable disposable = service.thirtyDays()
                        // tells Rx to request the data on a background Thread
                        .subscribeOn(Schedulers.io())
                        // tells Rx to handle the response on Swing's main Thread
                        .observeOn(SwingSchedulers.edt())
                        //.observeOn(AndroidSchedulers.mainThread()) // Instead use this on Android only
                        .subscribe(
                                (response) -> handleResponse(response),
                                Throwable::printStackTrace);
            }
        });

        jlist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = jlist.getSelectedIndex();
                    if (selectedIndex != -1) {
                        Feature selectedFeature = getFeatureAt(selectedIndex);
                        if (selectedFeature != null) {
                            openGoogleMaps(selectedFeature.geometry.coordinates[1],
                                    selectedFeature.geometry.coordinates[0]);
                        }
                    }
                }
            }
        });

    }

    private Feature getFeatureAt(int index) {
        if (index >= 0 && index < jlist.getModel().getSize()) {
            String selectedValue = jlist.getModel().getElementAt(index);
            String[] parts = selectedValue.split(" ");
            if (parts.length >= 2) {
                double mag = Double.parseDouble(parts[0]);
                String place = selectedValue.substring(String.valueOf(mag).length() + 1);
                for (Feature feature : getLastFeatures()) {
                    if (feature.properties.mag == mag && feature.properties.place.equals(place)) {
                        return feature;
                    }
                }
            }
        }
        return null;
    }

    private void openGoogleMaps(double latitude, double longitude) {
        try {
            String url = "https://www.google.com/maps?q=" + latitude + "," + longitude;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Feature[] getLastFeatures() {
        // Return features from the last response received
        // Modify this method as per your implementation
        return null;
    }

    private void handleResponse(FeatureCollection response) {
        String[] listData = Arrays.stream(response.features)
                .map(feature -> feature.properties.mag + " " + feature.properties.place)
                .toList()
                .toArray(new String[0]);
        jlist.setListData(listData);
    }

}
