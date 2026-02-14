import pandas as pd
import matplotlib.pyplot as plt
import os

csv_path = 'results/metrics.csv'

if not os.path.exists(csv_path):
    print(f"Error: {csv_path} not found.")
else:
    df = pd.read_csv(csv_path)
    df = df[df['status'] == 'OK'].copy()
    
    start_time = df['timestamp'].min()
    df['rel_sec'] = (df['timestamp'] - start_time) / 1000.0
    df['bucket_10s'] = (df['rel_sec'] // 10) * 10
    
    throughput_10s = df.groupby('bucket_10s').size() / 10.0

    plt.figure(figsize=(12, 6))
    plt.plot(throughput_10s.index, throughput_10s.values, marker='o', color='red')
    plt.title('Throughput Over Time (10-Second Buckets)')
    plt.xlabel('Elapsed Time (seconds)')
    plt.ylabel('Throughput (Avg msg/s)')
    plt.grid(True)
    plt.savefig('throughput_10s.png')
    
    latency_ms = df['latencyMs'] 
    print(f"P95 Latency: {latency_ms.quantile(0.95):.2f} ms")
    print(f"P99 Latency: {latency_ms.quantile(0.99):.2f} ms")
    print(f"Mean Latency: {latency_ms.mean():.2f} ms")