import { cookies } from "next/headers";
import type { ApiEnvelope, UserProfile } from "@/lib/types";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function getCurrentUser(): Promise<UserProfile | null> {
  try {
    	const cookieStore = await cookies();
	const cookieHeader = cookieStore.toString();
    if (!cookieHeader) {
      return null;
    }

    const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
      headers: { Cookie: cookieHeader, Accept: "application/json" },
      cache: "no-store",
    });

    if (!response.ok) {
      return null;
    }

    const json = (await response.json()) as ApiEnvelope<UserProfile>;
    return json.success ? json.data : null;
  } catch {
    return null;
  }
}