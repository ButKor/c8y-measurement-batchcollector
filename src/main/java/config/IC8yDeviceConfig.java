package config;

public interface IC8yDeviceConfig {
  Boolean activate();
  String type();
  String name();
  String externalIdKey();
  String externalIdValue();
}