package app.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Setter
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
@Embeddable
public class Coordenada {
	@NotNull
	private Double latitude;
	@NotNull
	private Double longitude;
	
	public long distanciaAte(Coordenada destino) {

        double dLat = Math.abs(this.latitude - destino.latitude);
        double dLon = Math.abs(this.longitude - destino.longitude);

        if (dLat < 0.1 && dLon < 0.1) {
            return distanciaEquiretangular(destino);
        }

        return distanciaHaversine(destino);
    }
	
	private long distanciaEquiretangular(Coordenada destino) {

        final double R = 6371000;

        double x = Math.toRadians(destino.longitude - this.longitude)
                * Math.cos(Math.toRadians((this.latitude + destino.latitude) / 2));

        double y = Math.toRadians(destino.latitude - this.latitude);

        return Math.round(Math.sqrt(x * x + y * y) * R);
    }
	
	
	private long distanciaHaversine(Coordenada destino) {

        final double R = 6371000;

        double dLat = Math.toRadians(destino.latitude - this.latitude);
        double dLon = Math.toRadians(destino.longitude - this.longitude);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(destino.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c);
    }

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Coordenada(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordenada() {
		super();
	}
	
	
}
