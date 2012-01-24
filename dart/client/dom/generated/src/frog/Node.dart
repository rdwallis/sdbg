
class NodeJS implements Node native "*Node" {

  static final int ATTRIBUTE_NODE = 2;

  static final int CDATA_SECTION_NODE = 4;

  static final int COMMENT_NODE = 8;

  static final int DOCUMENT_FRAGMENT_NODE = 11;

  static final int DOCUMENT_NODE = 9;

  static final int DOCUMENT_POSITION_CONTAINED_BY = 0x10;

  static final int DOCUMENT_POSITION_CONTAINS = 0x08;

  static final int DOCUMENT_POSITION_DISCONNECTED = 0x01;

  static final int DOCUMENT_POSITION_FOLLOWING = 0x04;

  static final int DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;

  static final int DOCUMENT_POSITION_PRECEDING = 0x02;

  static final int DOCUMENT_TYPE_NODE = 10;

  static final int ELEMENT_NODE = 1;

  static final int ENTITY_NODE = 6;

  static final int ENTITY_REFERENCE_NODE = 5;

  static final int NOTATION_NODE = 12;

  static final int PROCESSING_INSTRUCTION_NODE = 7;

  static final int TEXT_NODE = 3;

  NamedNodeMapJS get attributes() native "return this.attributes;";

  String get baseURI() native "return this.baseURI;";

  NodeListJS get childNodes() native "return this.childNodes;";

  NodeJS get firstChild() native "return this.firstChild;";

  NodeJS get lastChild() native "return this.lastChild;";

  String get localName() native "return this.localName;";

  String get namespaceURI() native "return this.namespaceURI;";

  NodeJS get nextSibling() native "return this.nextSibling;";

  String get nodeName() native "return this.nodeName;";

  int get nodeType() native "return this.nodeType;";

  String get nodeValue() native "return this.nodeValue;";

  void set nodeValue(String value) native "this.nodeValue = value;";

  DocumentJS get ownerDocument() native "return this.ownerDocument;";

  ElementJS get parentElement() native "return this.parentElement;";

  NodeJS get parentNode() native "return this.parentNode;";

  String get prefix() native "return this.prefix;";

  void set prefix(String value) native "this.prefix = value;";

  NodeJS get previousSibling() native "return this.previousSibling;";

  String get textContent() native "return this.textContent;";

  void set textContent(String value) native "this.textContent = value;";

  void addEventListener(String type, EventListener listener, [bool useCapture = null]) native;

  NodeJS appendChild(NodeJS newChild) native;

  NodeJS cloneNode(bool deep) native;

  int compareDocumentPosition(NodeJS other) native;

  bool contains(NodeJS other) native;

  bool dispatchEvent(EventJS event) native;

  bool hasAttributes() native;

  bool hasChildNodes() native;

  NodeJS insertBefore(NodeJS newChild, NodeJS refChild) native;

  bool isDefaultNamespace(String namespaceURI) native;

  bool isEqualNode(NodeJS other) native;

  bool isSameNode(NodeJS other) native;

  bool isSupported(String feature, String version) native;

  String lookupNamespaceURI(String prefix) native;

  String lookupPrefix(String namespaceURI) native;

  void normalize() native;

  NodeJS removeChild(NodeJS oldChild) native;

  void removeEventListener(String type, EventListener listener, [bool useCapture = null]) native;

  NodeJS replaceChild(NodeJS newChild, NodeJS oldChild) native;

  var dartObjectLocalStorage;

  String get typeName() native;
}
