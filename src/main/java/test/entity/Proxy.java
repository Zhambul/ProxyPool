package test.entity;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by 10 on 18.04.2016.
 */
@Entity
public class Proxy {
    private Integer id;
    private String ip;
    private boolean isActive;
    private Date lastRequestDate;
    private int rating;
    private Timestamp requestTime;
    private int port;
    private String scheme;
    private String login;
    private String password;
    private String country;
    private String city;
    private static final int minRating = -20;
    private static final int maxRating = 20;

    public void incRating() {
        int currentRating = getRating();
        if(currentRating < maxRating) {
            setRating(++currentRating);
        }
    }

    public void decRating() {
        int currentRating = getRating();
        if(currentRating > minRating) {
            setRating(--currentRating);
        }
    }

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "ip", nullable = true, length = 50)
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "is_active", nullable = true)
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Basic
    @Column(name = "last_request_date", nullable = true)
    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(Date lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    @Basic
    @Column(name = "rating", nullable = true)
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Basic
    @Column(name = "request_time", nullable = true)
    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    @Basic
    @Column(name = "port", nullable = true)
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Basic
    @Column(name = "scheme", nullable = true, length = 10)
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Basic
    @Column(name = "login", nullable = true, length = 50)
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Basic
    @Column(name = "password", nullable = true, length = 50)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "country", nullable = true, length = 50)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Basic
    @Column(name = "city", nullable = true, length = 50)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Proxy proxy = (Proxy) o;

        if (isActive != proxy.isActive) return false;
        if (rating != proxy.rating) return false;
        if (port != proxy.port) return false;
        if (id != null ? !id.equals(proxy.id) : proxy.id != null) return false;
        if (ip != null ? !ip.equals(proxy.ip) : proxy.ip != null) return false;
        if (lastRequestDate != null ? !lastRequestDate.equals(proxy.lastRequestDate) : proxy.lastRequestDate != null)
            return false;
        if (requestTime != null ? !requestTime.equals(proxy.requestTime) : proxy.requestTime != null) return false;
        if (scheme != null ? !scheme.equals(proxy.scheme) : proxy.scheme != null) return false;
        if (login != null ? !login.equals(proxy.login) : proxy.login != null) return false;
        if (password != null ? !password.equals(proxy.password) : proxy.password != null) return false;
        if (country != null ? !country.equals(proxy.country) : proxy.country != null) return false;
        if (city != null ? !city.equals(proxy.city) : proxy.city != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (isActive ? 1 : 0);
        result = 31 * result + (lastRequestDate != null ? lastRequestDate.hashCode() : 0);
        result = 31 * result + rating;
        result = 31 * result + (requestTime != null ? requestTime.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        return result;
    }
}
