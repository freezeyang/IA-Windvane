package sensor.information.collector;

public class Contact {
	// private variables
	int _id;
	String _name;
	String _phone_number;
	String _longitude;
	String _latitude;
	String _address;
	String _timestamp;
	String _xtext;
	String _ytext;
	String _ztext;
	String _lightreading;
	String _touchscreen;
	String _useridtrue;
	String _useridwindvane;

	// Empty constructor
	public Contact() {

	}

	// constructor
	public Contact(int id, String name, String _phone_number,String _longitude,String _latitude, String _address,
			String _xtext, String _ytext, String _ztext, String _lightreading,
			String _touchscreen, String _useridtrue, String _useridwindvane, String _timestamp) {
		this._id = id;
		this._name = name;
		this._phone_number = _phone_number;
		this._longitude=_longitude;
		this._latitude=_latitude;
		this._address = _address;
		this._xtext = _xtext;
		this._ytext = _ytext;
		this._ztext = _ztext;
		this._lightreading = _lightreading;
		this._touchscreen = _touchscreen;
		this._useridtrue=_useridtrue;
		this._useridwindvane=_useridwindvane;
		this._timestamp = _timestamp;
	}

	// constructor
	public Contact(String name, String _phone_number,String _longitude,String _latitude, String _address,
			String _xtext, String _ytext, String _ztext, String _lightreading,
			String _touchscreen, String _useridtrue, String _useridwindvane, String _timestamp) {
		this._name = name;
		this._phone_number = _phone_number;
		this._longitude=_longitude;
		this._latitude=_latitude;
		this._address = _address;
		this._xtext = _xtext;
		this._ytext = _ytext;
		this._ztext = _ztext;
		this._lightreading = _lightreading;
		this._touchscreen = _touchscreen;
		this._useridtrue =_useridtrue;
		this._useridwindvane=_useridwindvane;
		this._timestamp = _timestamp;
	}

	// getting ID
	public int getID() {
		return this._id;
	}

	// setting id
	public void setID(int id) {
		this._id = id;
	}

	// getting name
	public String getName() {
		return this._name;
	}

	// setting name
	public void setName(String name) {
		this._name = name;
	}

	// getting phone number
	public String getPhoneNumber() {
		return this._phone_number;
	}

	// setting phone number
	public void setPhoneNumber(String phone_number) {
		this._phone_number = phone_number;
	}

	// get longitude
	public String getLongitude(){return this._longitude;}

	//set longitude
	public void set_Longitude(String longitude){this._longitude=longitude;}

	//get latitude
	public String getLatitude(){return this._latitude;}

	//set latitude
	public void set_Latitude(String latitude){this._latitude=latitude;}

	// getting Address
	public String getAddress() {
		return this._address;
	}

	// setting Address
	public void setAddress(String address) {
		this._address = address;
	}

	// getting XText

	public String getXText() {
		return this._xtext;
	}

	// setting XText
	public void setXText(String _xtext) {
		this._xtext = _xtext;
	}

	// getting YText

	public String getYText() {
		return this._ytext;
	}

	// setting YText
	public void setYText(String _ytext) {
		this._ytext = _ytext;
	}

	// getting ZText

	public String getZText() {
		return this._ztext;
	}

	// setting ZText
	public void setZText(String _ztext) {
		this._ztext = _ztext;
	}

	// getting Light Reading

	public String getLightReading() {
		return this._lightreading;
	}

	// setting Light Reading
	public void setLightReading(String _lightreading) {
		this._lightreading = _lightreading;
	}

	// getting Touch Screen Text

	public String getTouchScreen() {
		return this._touchscreen;
	}

	// setting Touch Screen Text
	public void setTouchScreen(String _touchscreen) {
		this._touchscreen = _touchscreen;
	}

	// getting true user id
	public String getUserIDTrue(){
		return this._useridtrue;
	}

	// setting true user id
	public void setUserIDTrue(String _useridtrue){
		this._useridtrue=_useridtrue;
	}

	// getting windvane user id
	public String getUserIDWindVane(){
		return this._useridwindvane;
	}

	// setting windvane user id
	public void setUserIDWindVane(String _useridwindvane){
		this._useridwindvane=_useridwindvane;
	}

	// getting timestamp
	public String getTimeStamp() {
		return this._timestamp;
	}

	// setting timestamp
	public void setTimeStamp(String timestamp) {
		this._timestamp = timestamp;
	}
}
