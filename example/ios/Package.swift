// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Authentication",
    targets: [
        .target(name: "Authentication"),
        .testTarget(name: "AuthenticationTests", dependencies: ["Authentication"]),
    ]
)
