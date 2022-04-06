package csvmeasurementcollector.config;

public interface IRequestConfig {
  String dateFrom();
  String dateTo();
  String source();
  Integer chunkSize();
}