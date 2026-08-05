// LoginPage.jsx – Secure login with strong password and biometric verification
import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert, ActivityIndicator, Image, KeyboardAvoidingView, Platform } from 'react-native';
import { useAuth } from './contexts';
import { useTranslation } from './LanguageContext';
import { Eye, EyeOff, Fingerprint, Key } from 'lucide-react-native';
import { NativeModules } from 'react-native';

// Native bridge for biometric helper (if available)
const { BiometricAuthHelper } = NativeModules;

export default function LoginPage({ navigation }) {
  const { login, user } = useAuth();
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [biometricAvailable, setBiometricAvailable] = useState(false);
  const [biometricType, setBiometricType] = useState('');

  // Check biometric availability on mount
  useEffect(() => {
    if (BiometricAuthHelper) {
      BiometricAuthHelper.isBiometricAvailable()
        .then((result) => {
          setBiometricAvailable(result.available);
          setBiometricType(result.biometryType || 'biometric');
        })
        .catch(() => setBiometricAvailable(false));
    }
  }, []);

  // Validate strong password (at least 8 chars, uppercase, lowercase, number, special)
  const validatePassword = (pass) => {
    const minLength = 8;
    const hasUpper = /[A-Z]/.test(pass);
    const hasLower = /[a-z]/.test(pass);
    const hasNumber = /[0-9]/.test(pass);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(pass);
    return pass.length >= minLength && hasUpper && hasLower && hasNumber && hasSpecial;
  };

  // Handle login
  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert(t('error'), t('fill_all_fields'));
      return;
    }

    setLoading(true);
    try {
      // Step 1: Authenticate with email/password
      const authResult = await login(email, password);
      if (authResult.success) {
        // Step 2: If biometric is available and enabled for the user, verify
        if (biometricAvailable && authResult.user.biometricEnabled) {
          const bioResult = await BiometricAuthHelper.authenticate({
            title: t('biometric_verify_title') || 'Verify Identity',
            subtitle: t('biometric_verify_sub') || 'Use your fingerprint or face to confirm',
            description: t('biometric_verify_desc') || 'For security, please verify your identity.',
          });
          if (!bioResult.success) {
            Alert.alert(t('auth_failed'), t('biometric_verification_failed'));
            return; // stop login
          }
        }
        // Success – navigate to home
        navigation?.replace('Home');
      } else {
        Alert.alert(t('auth_failed'), authResult.message || t('invalid_credentials'));
      }
    } catch (error) {
      Alert.alert(t('error'), error.message || t('something_wrong'));
    } finally {
      setLoading(false);
    }
  };

  // Handle biometric quick login (if saved)
  const handleBiometricLogin = async () => {
    if (!biometricAvailable) return;
    setLoading(true);
    try {
      const bioResult = await BiometricAuthHelper.authenticate({
        title: 'Login with ' + biometricType,
        subtitle: 'Place your finger or look at the camera',
        description: 'Verify your identity to log in automatically.',
      });
      if (bioResult.success) {
        // If we have stored credentials, use them to login
        // Here we would retrieve stored credentials from secure storage
        // For now, we just simulate; you can implement with react-native-keychain
        const storedUser = await getStoredCredentials(); // implement this
        if (storedUser) {
          const authResult = await login(storedUser.email, storedUser.password, { biometricVerified: true });
          if (authResult.success) {
            navigation?.replace('Home');
          }
        } else {
          Alert.alert(t('error'), t('no_stored_credentials'));
        }
      }
    } catch (error) {
      Alert.alert(t('error'), error.message || t('biometric_failed'));
    } finally {
      setLoading(false);
    }
  };

  // Placeholder for retrieving stored credentials (use react-native-keychain)
  const getStoredCredentials = async () => {
    // Implement secure storage retrieval
    return null;
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      className="flex-1 bg-[#090d16] justify-center px-6"
    >
      <View className="items-center mb-8">
        <Image
          source={require('../assets/shebaodds_logo.png')}
          style={{ width: 180, height: 60, resizeMode: 'contain' }}
        />
        <Text className="text-amber-400 text-lg font-bold mt-2">Smart Bets. Real Wins.</Text>
      </View>

      <View className="bg-[#111625] p-6 rounded-2xl border border-slate-800">
        <Text className="text-white text-2xl font-bold mb-2">{t('welcome_back') || 'Welcome Back'}</Text>
        <Text className="text-slate-400 mb-6">{t('login_sub') || 'Sign in to access your account'}</Text>

        {/* Email input */}
        <View className="mb-4">
          <Text className="text-slate-300 text-sm mb-1">{t('email') || 'Email'}</Text>
          <TextInput
            className="bg-[#090d16] border border-slate-800 rounded-lg px-4 py-3 text-white"
            placeholder="you@example.com"
            placeholderTextColor="#64748b"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
        </View>

        {/* Password input */}
        <View className="mb-6">
          <Text className="text-slate-300 text-sm mb-1">{t('password') || 'Password'}</Text>
          <View className="flex-row items-center bg-[#090d16] border border-slate-800 rounded-lg">
            <TextInput
              className="flex-1 px-4 py-3 text-white"
              placeholder={t('enter_password') || 'Enter your password'}
              placeholderTextColor="#64748b"
              secureTextEntry={!showPassword}
              value={password}
              onChangeText={setPassword}
            />
            <TouchableOpacity onPress={() => setShowPassword(!showPassword)} className="pr-3">
              {showPassword ? <EyeOff size={20} color="#64748b" /> : <Eye size={20} color="#64748b" />}
            </TouchableOpacity>
          </View>
          <Text className="text-xs text-slate-500 mt-1">
            {t('password_requirements') || 'Min 8 chars, upper, lower, number, special'}
          </Text>
        </View>

        {/* Login button */}
        <TouchableOpacity
          className={`bg-amber-500 py-4 rounded-lg items-center ${loading ? 'opacity-50' : ''}`}
          onPress={handleLogin}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#090d16" />
          ) : (
            <Text className="text-[#090d16] font-bold text-base">{t('login') || 'Login'}</Text>
          )}
        </TouchableOpacity>

        {/* Biometric quick login */}
        {biometricAvailable && (
          <TouchableOpacity
            className="mt-4 flex-row items-center justify-center border border-slate-800 py-3 rounded-lg"
            onPress={handleBiometricLogin}
            disabled={loading}
          >
            <Fingerprint size={20} color="#f0b90b" />
            <Text className="text-amber-400 font-semibold ml-2">
              {t('login_with_biometric') || `Login with ${biometricType}`}
            </Text>
          </TouchableOpacity>
        )}

        {/* Forgot password / Register links */}
        <View className="flex-row justify-between mt-6">
          <TouchableOpacity onPress={() => navigation?.navigate('ResetPassword')}>
            <Text className="text-amber-400 text-sm">{t('forgot_password') || 'Forgot password?'}</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation?.navigate('Register')}>
            <Text className="text-amber-400 text-sm">{t('create_account') || 'Create account'}</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Footer note */}
      <Text className="text-slate-500 text-xs text-center mt-6">
        {t('secure_login_note') || '🔒 Your data is SSL encrypted and secured'}
      </Text>
    </KeyboardAvoidingView>
  );
}