package made.by.human.tiktokantiburn;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeManager implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F; // обычное встряхивание
    private static final float ROTATE_THRESHOLD = 3.0F;        // порог вращения (рад/с)
    private static final int SHAKE_SLOP_TIME_MS = 500;

    private long mShakeTimestamp;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor gyroscope;
    private final Runnable onShake;

    public ShakeManager(Activity activity, Runnable onShake) {
        this.onShake = onShake;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void start() {
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (gyroscope != null)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return;
                mShakeTimestamp = now;
                if (onShake != null) onShake.run();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float rotX = event.values[0];
            float rotY = event.values[1];
            float rotZ = event.values[2];

            float rotation = (float) Math.sqrt(rotX * rotX + rotY * rotY + rotZ * rotZ);

            if (rotation > ROTATE_THRESHOLD) {
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return;
                mShakeTimestamp = now;
                if (onShake != null) onShake.run();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
