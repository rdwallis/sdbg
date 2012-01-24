
class IDBDatabaseJS implements IDBDatabase native "*IDBDatabase" {

  String get name() native "return this.name;";

  EventListener get onabort() native "return this.onabort;";

  void set onabort(EventListener value) native "this.onabort = value;";

  EventListener get onerror() native "return this.onerror;";

  void set onerror(EventListener value) native "this.onerror = value;";

  EventListener get onversionchange() native "return this.onversionchange;";

  void set onversionchange(EventListener value) native "this.onversionchange = value;";

  String get version() native "return this.version;";

  void addEventListener(String type, EventListener listener, [bool useCapture = null]) native;

  void close() native;

  IDBObjectStoreJS createObjectStore(String name) native;

  void deleteObjectStore(String name) native;

  bool dispatchEvent(EventJS evt) native;

  void removeEventListener(String type, EventListener listener, [bool useCapture = null]) native;

  IDBVersionChangeRequestJS setVersion(String version) native;

  IDBTransactionJS transaction(String storeName, int mode) native;

  var dartObjectLocalStorage;

  String get typeName() native;
}
