package io.bclub.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class UserInfo implements Parcelable {

    public final String email;

    public final String name;
    public final String surname;

    public final String cpf;
    public final Date birthdate;
    public final String telephone;

    public final String voucher;

    public final String address;
    public final String complement;
    public final String zipcode;
    public final String state;
    public final String city;

    @Plan
    public final String plan;

    public UserInfo(String name, String surname, String email, String cpf, Date birthdate, String telephone, String voucher, String plan, String address, String complement, String zipcode, String city, String state) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.cpf = cpf;
        this.birthdate = birthdate;
        this.telephone = telephone;
        this.voucher = voucher;
        this.plan = plan;

        this.address = address;
        this.complement = complement;
        this.zipcode = zipcode;
        this.state = state;
        this.city = city;
    }

    protected UserInfo(Parcel in) {
        name = in.readString();
        surname = in.readString();
        email = in.readString();
        cpf = in.readString();
        telephone = in.readString();

        address = in.readString();
        complement = in.readString();
        zipcode = in.readString();
        state = in.readString();
        city = in.readString();

        voucher = in.readString();

        //noinspection WrongConstant
        plan = in.readString();

        long timestamp = in.readLong();

        if (timestamp != -1L) {
            birthdate = new Date();
            birthdate.setTime(timestamp);
        } else {
            birthdate = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(email);
        dest.writeString(cpf);
        dest.writeString(telephone);

        dest.writeString(address);
        dest.writeString(complement);
        dest.writeString(zipcode);
        dest.writeString(state);
        dest.writeString(city);

        dest.writeString(voucher);

        dest.writeString(plan);

        if (birthdate != null) {
            dest.writeLong(birthdate.getTime());
        } else {
            dest.writeLong(-1L);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
